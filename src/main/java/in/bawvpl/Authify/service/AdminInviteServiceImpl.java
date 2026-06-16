package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.AdminInviteEntity;
import in.bawvpl.Authify.entity.AdminRole;
import in.bawvpl.Authify.entity.UserEntity;
import in.bawvpl.Authify.entity.UserStatus;
import in.bawvpl.Authify.io.AdminInviteCompleteRequest;
import in.bawvpl.Authify.io.AdminInviteInfoResponse;
import in.bawvpl.Authify.io.AdminInviteRequest;
import in.bawvpl.Authify.repository.AdminInviteRepository;
import in.bawvpl.Authify.repository.UserRepository;
import in.bawvpl.Authify.util.AdminOtpPurpose;
import in.bawvpl.Authify.util.InviteTokenUtil;
import in.bawvpl.Authify.util.RoleValidator;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminInviteServiceImpl
        implements AdminInviteService {

    private final AdminInviteRepository adminInviteRepository;

    private final UserRepository userRepository;

    private final RoleValidator roleValidator;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final AdminOtpVerificationService adminOtpVerificationService;
    private final AdminAuditLogService adminAuditLogService;
    private final EmailService emailService;

    @Value("${auth.frontend.base-url:http://localhost:3000}")
    private String frontendBaseUrl;

    @Override
    public void requestInviteOtp(
            UserEntity inviter
    ) {

        otpService.generateAdminInviteOtp(
                inviter
        );
    }

    @Override
    public void verifyInviteOtp(
            UserEntity inviter,
            String otp
    ) {

        otpService.verifyAdminInviteOtp(
                inviter,
                otp
        );

        adminOtpVerificationService.markVerified(
                inviter.getId(),
                AdminOtpPurpose.ADMIN_INVITE_ACTION
        );
    }

    @Override
    public void createInvite(
            UserEntity inviter,
            AdminInviteRequest request
    ) {

        validateInvitePermission(
                inviter,
                request.getRole()
        );

        if (!adminOtpVerificationService.isVerified(
                inviter.getId(),
                AdminOtpPurpose.ADMIN_INVITE_ACTION
        )) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "OTP verification required"
            );
        }

        List<AdminInviteEntity> activeInvites =
                adminInviteRepository
                        .findByEmailIgnoreCaseAndUsedFalse(
                                request.getEmail()
                        );

        for (AdminInviteEntity invite : activeInvites) {

            invite.setUsed(true);
        }

        adminInviteRepository.saveAll(
                activeInvites
        );

        String rawToken =
                InviteTokenUtil.generateToken();

        String tokenHash =
                InviteTokenUtil.sha256(
                        rawToken
                );

        AdminRole role =
                AdminRole.valueOf(
                        "ROLE_" +
                                request.getRole()
                                        .trim()
                                        .toUpperCase()
                );

        AdminInviteEntity invite =
                AdminInviteEntity.builder()

                        .email(
                                request.getEmail()
                        )

                        .fullName(
                                request.getFullName()
                        )

                        .role(
                                role
                        )

                        .tokenHash(
                                tokenHash
                        )

                        .invitedBy(
                                inviter.getId()
                        )

                        .expiresAt(
                                LocalDateTime.now()
                                        .plusHours(24)
                        )

                        .build();

        adminInviteRepository.save(
                invite
        );

        // ✅ AUDIT TRAIL LOGGING
        adminAuditLogService.logInviteCreated(
                inviter.getId(),
                request.getEmail(),
                role.name()
        );

        adminOtpVerificationService.consumeVerification(
                inviter.getId(),
                AdminOtpPurpose.ADMIN_INVITE_ACTION
        );

        // ✅ EMAIL DISPATCH IMPLEMENTATION
        String inviteLink =
                frontendBaseUrl +
                        "/admin/invite/" +
                        rawToken;

        emailService.sendAdminInvite(
                request.getEmail(),
                request.getFullName(),
                inviteLink,
                role.name()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AdminInviteInfoResponse getInviteInfo(
            String token
    ) {

        String hash =
                InviteTokenUtil.sha256(
                        token
                );

        AdminInviteEntity invite =
                adminInviteRepository
                        .findByTokenHash(hash)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Invite not found"
                                )
                        );

        boolean expired =
                invite.getExpiresAt()
                        .isBefore(
                                LocalDateTime.now()
                        );

        return AdminInviteInfoResponse
                .builder()
                .email(
                        invite.getEmail()
                )
                .fullName(
                        invite.getFullName()
                )
                .role(
                        invite.getRole().name()
                )
                .expired(
                        expired
                )
                .used(
                        invite.getUsed()
                )
                .build();
    }

    @Override
    public void completeInvite(
            AdminInviteCompleteRequest request
    ) {

        String hash =
                InviteTokenUtil.sha256(
                        request.getToken()
                );

        AdminInviteEntity invite =
                adminInviteRepository
                        .findByTokenHash(hash)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Invite not found"
                                )
                        );

        if (Boolean.TRUE.equals(invite.getUsed())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invite already used"
            );
        }

        if (
                invite.getExpiresAt()
                        .isBefore(
                                LocalDateTime.now()
                        )
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invite expired"
            );
        }

        if (
                userRepository
                        .existsByEmailIgnoreCase(
                                invite.getEmail()
                        )
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Account already exists"
            );
        }

        UserEntity user =
                UserEntity.builder()

                        .userStatus(
                                UserStatus.ACTIVE
                        )

                        .email(
                                invite.getEmail()
                        )

                        .entityName(
                                invite.getFullName()
                        )

                        .adminRole(
                                invite.getRole()
                        )

                        .emailVerified(
                                true
                        )

                        .phoneVerified(
                                false
                        )

                        .build();

        user.setPassword(
                passwordEncoder.encode(
                        request.getPassword()
                )
        );

        user.setPhoneNumber(
                request.getPhoneNumber()
        );

        userRepository.save(
                user
        );

        invite.setUsed(true);

        adminInviteRepository.save(
                invite
        );
    }

    private void validateInvitePermission(
            UserEntity inviter,
            String requestedRole
    ) {

        if (!roleValidator.isAdminOrOwner(inviter)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Access denied"
            );
        }

        if (requestedRole == null || requestedRole.isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Role is required"
            );
        }

        String role =
                requestedRole
                        .trim()
                        .toUpperCase();

        // ADMIN cannot invite OWNER
        if (
                roleValidator.isAdmin(inviter)
                        &&
                        "OWNER".equals(role)
        ) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "ADMIN cannot invite OWNER"
            );
        }

        if (
                !"ADMIN".equals(role)
                        &&
                        !"OWNER".equals(role)
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid role"
            );
        }
    }
}