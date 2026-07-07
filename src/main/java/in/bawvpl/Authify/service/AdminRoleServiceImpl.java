package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.AdminRole;
import in.bawvpl.Authify.entity.UserEntity;
import in.bawvpl.Authify.repository.UserRepository;
import in.bawvpl.Authify.repository.UserSessionRepository;
import in.bawvpl.Authify.util.AdminOtpPurpose;
import in.bawvpl.Authify.util.RoleValidator;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminRoleServiceImpl implements AdminRoleService {

    private final UserRepository userRepository;
    private final RoleValidator roleValidator;
    private final OtpService otpService;
    private final AdminOtpVerificationService adminOtpVerificationService;
    private final AdminAuditLogService adminAuditLogService;
    private final UserSessionRepository userSessionRepository;

    @Override
    public void requestRoleChangeOtp(UserEntity requestor) {
        if (!roleValidator.isAdminOrOwner(requestor)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Access denied"
            );
        }

        // Generate and dispatch OTP
        otpService.generateRoleChangeOtp(requestor);
    }

    @Override
    public void verifyRoleChangeOtp(UserEntity requestor, String otp) {
        if (!roleValidator.isAdminOrOwner(requestor)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Access denied"
            );
        }

        // Validate OTP signature
        otpService.verifyRoleChangeOtp(
                requestor,
                otp
        );

        String actionToken =
                java.util.UUID.randomUUID().toString();

        adminOtpVerificationService.markVerified(
                requestor.getId(),
                AdminOtpPurpose.ROLE_CHANGE_ACTION,
                actionToken
        );
    }

    @Override
    public void changeRole(
            UserEntity requestor,
            String targetUserId,
            String newRole
    ){
        UserEntity target =
                userRepository.findByUserId(targetUserId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));

        // ✅ SECURITY: Enforce OTP Verification Clearance Before Executing State Operations
        if (!adminOtpVerificationService.isVerified(
                requestor.getId(),
                AdminOtpPurpose.ROLE_CHANGE_ACTION
        )) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "OTP verification required"
            );
        }

        // ✅ SELF-PROTECTION GUARD
        if (requestor.getId().equals(target.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Self role change not allowed"
            );
        }

        if (newRole == null || newRole.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Role required"
            );
        }

        String normalizedRole = newRole.trim().toUpperCase();
        if (!normalizedRole.startsWith("ROLE_")) {
            normalizedRole = "ROLE_" + normalizedRole;
        }

        AdminRole role;
        try {
            role = AdminRole.valueOf(normalizedRole);
        } catch (Exception ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid role"
            );
        }

        // ✅ LAST OWNER PROTECTION GUARD
        if (target.getAdminRole() == AdminRole.ROLE_OWNER && role != AdminRole.ROLE_OWNER) {
            long ownerCount = userRepository.countByAdminRole(AdminRole.ROLE_OWNER);
            if (ownerCount <= 1) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Cannot demote last OWNER"
                );
            }
        }

        // =====================================================
        // OWNER EXECUTION PRIVILEGES
        // =====================================================
        if (roleValidator.isOwner(requestor)) {
            String oldRole = target.getAdminRole().name();
            target.setAdminRole(role);

            // Null-safe token version incrementation to invalidate active JWT elements
            target.setTokenVersion(
                    (target.getTokenVersion() == null ? 0 : target.getTokenVersion()) + 1
            );
            userRepository.save(target);

            // Deactivate all persistent server-side active user sessions
            userSessionRepository.deactivateAllByUserId(target.getId());

            // Audit Trail Documentation
            adminAuditLogService.logRoleChange(
                    requestor.getId(),
                    target.getId(),
                    oldRole,
                    role.name()
            );

            // Invalidate/Consume used verification tokens
            adminOtpVerificationService.consumeVerification(
                    requestor.getId(),
                    AdminOtpPurpose.ROLE_CHANGE_ACTION
            );
            return;
        }

        // =====================================================
        // ADMIN EXECUTION PRIVILEGES
        // =====================================================
        if (roleValidator.isAdmin(requestor)) {
            if (roleValidator.isOwner(target)) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Cannot modify OWNER"
                );
            }

            if (role == AdminRole.ROLE_OWNER) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Cannot assign OWNER"
                );
            }

            String oldRole = target.getAdminRole().name();
            target.setAdminRole(role);

            // Null-safe token version incrementation to invalidate active JWT elements
            target.setTokenVersion(
                    (target.getTokenVersion() == null ? 0 : target.getTokenVersion()) + 1
            );
            userRepository.save(target);

            // Deactivate all persistent server-side active user sessions
            userSessionRepository.deactivateAllByUserId(target.getId());

            // Audit Trail Documentation
            adminAuditLogService.logRoleChange(
                    requestor.getId(),
                    target.getId(),
                    oldRole,
                    role.name()
            );

            // Invalidate/Consume used verification tokens
            adminOtpVerificationService.consumeVerification(
                    requestor.getId(),
                    AdminOtpPurpose.ROLE_CHANGE_ACTION
            );
            return;
        }

        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Access denied"
        );
    }
}