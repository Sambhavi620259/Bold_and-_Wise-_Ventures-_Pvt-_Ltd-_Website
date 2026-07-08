package in.bawvpl.Authify.service;

import in.bawvpl.Authify.config.AdminManagementException;
import in.bawvpl.Authify.config.PrivilegeEscalationException;
import in.bawvpl.Authify.config.UnauthorizedRoleException;
import in.bawvpl.Authify.entity.UserEntity;
import in.bawvpl.Authify.entity.AdminRole;
import in.bawvpl.Authify.entity.UserStatus;
import in.bawvpl.Authify.io.*;
import in.bawvpl.Authify.repository.UserRepository;
import in.bawvpl.Authify.util.AuditLogger;
import in.bawvpl.Authify.util.PermissionChecker;
import in.bawvpl.Authify.util.RoleValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Core admin management service
 * CRITICAL: All operations are server-side authorized
 */
@Slf4j
@Service
@Transactional
public class AdminManagementService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleValidator roleValidator;
    private final PermissionChecker permissionChecker;
    private final AuditLogger auditLogger;

    public AdminManagementService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            RoleValidator roleValidator,
            PermissionChecker permissionChecker,
            AuditLogger auditLogger) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleValidator = roleValidator;
        this.permissionChecker = permissionChecker;
        this.auditLogger = auditLogger;
    }

    /**
     * List all admins with pagination
     * Only SUPER_ADMIN and ADMIN can access
     */
    public AdminListResponse listAdmins(UserEntity requestor, Pageable pageable) {
        validateAccessToAdminEndpoint(requestor, "list admins");

        // FIX: Added repository call to retrieve paginated admin records
        Page<UserEntity> admins =
                userRepository.findAll(pageable);

        List<AdminResponse> content = admins.getContent()
                .stream()
                .map(this::convertToAdminResponse)
                .collect(Collectors.toList());

        return AdminListResponse.builder()
                .content(content)
                .totalPages(admins.getTotalPages())
                .totalElements(admins.getTotalElements())
                .currentPage(pageable.getPageNumber())
                .pageSize(pageable.getPageSize())
                .hasNext(admins.hasNext())
                .hasPrevious(admins.hasPrevious())
                .build();
    }

    /**
     * Create new admin
     * SUPER_ADMIN only
     * CRITICAL: Prevents privilege escalation
     */
    public AdminResponse createAdmin(UserEntity requestor, AdminCreateRequest request) {
        validateAccessToAdminEndpoint(requestor, "create admin");

        // CRITICAL: Only SUPER_ADMIN can create admins
        if (!roleValidator.canManageAdmins(requestor)) {
            throw new PrivilegeEscalationException("Only SUPER_ADMIN can create admins");
        }

        // CRITICAL: Prevent ROLE_SUPER_ADMIN creation via API
        String normalizedRoleStr = normalizeRole(request.getRole());
        if ("ROLE_SUPER_ADMIN".equalsIgnoreCase(normalizedRoleStr)) {
            auditLogger.logPrivilegeEscalationAttempt(requestor, "CREATE_SUPER_ADMIN");
            throw new PrivilegeEscalationException("SUPER_ADMIN role cannot be assigned via API");
        }

        // Convert normalized string role into your exact entity enum type
        AdminRole assignedRole;
        try {
            assignedRole = AdminRole.valueOf(normalizedRoleStr);
        } catch (IllegalArgumentException e) {
            throw new AdminManagementException("Invalid Admin Role provided: " + request.getRole());
        }

        // Check if email already exists
        if (userRepository.findByEmailIgnoreCase(request.getEmail()).isPresent()) {
            throw new AdminManagementException("Email already exists: " + request.getEmail());
        }

        // Create new admin using fields verified in your UserEntity model
        UserEntity newAdmin = UserEntity.builder()
                .email(request.getEmail().toLowerCase().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .adminRole(assignedRole) // Corrected from String type to AdminRole enum
                .contactPerson(request.getContactPerson())
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .entityName(request.getEntityName())
                .entityType(request.getEntityType())
                .emailVerified(request.getEmailVerified() != null && request.getEmailVerified())
                .userStatus(UserStatus.ACTIVE) // Maps status dynamically instead of isActive boolean flag
                .build();

        UserEntity savedAdmin = userRepository.save(newAdmin);
        auditLogger.logAdminCreated(requestor, savedAdmin);

        return convertToAdminResponse(savedAdmin);
    }

    /**
     * Update admin details
     * SUPER_ADMIN only
     */
    public AdminResponse updateAdmin(UserEntity requestor, Long adminId, AdminUpdateRequest request) {
        validateAccessToAdminEndpoint(requestor, "update admin");

        UserEntity admin = findAdminById(adminId);

        if (!permissionChecker.canUpdateAdmin(requestor, admin)) {
            throw new UnauthorizedRoleException("You do not have permission to update this admin");
        }

        // Track changes for audit logs
        StringBuilder changes = new StringBuilder();

        if (request.getContactPerson() != null && !request.getContactPerson().isBlank()) {
            changes.append("name: ").append(admin.getContactPerson()).append(" -> ").append(request.getContactPerson()).append("; ");
            admin.setContactPerson(request.getContactPerson());
        }

        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) {
            changes.append("phone: ").append(admin.getPhoneNumber()).append(" -> ").append(request.getPhoneNumber()).append("; ");
            admin.setPhoneNumber(request.getPhoneNumber());
        }

        if (request.getAddress() != null && !request.getAddress().isBlank()) {
            changes.append("address updated; ");
            admin.setAddress(request.getAddress());
        }

        if (request.getEntityName() != null && !request.getEntityName().isBlank()) {
            changes.append("entity: ").append(admin.getEntityName()).append(" -> ").append(request.getEntityName()).append("; ");
            admin.setEntityName(request.getEntityName());
        }

        if (request.getEmailVerified() != null) {
            changes.append("email_verified: ").append(admin.getEmailVerified()).append(" -> ").append(request.getEmailVerified()).append("; ");
            admin.setEmailVerified(request.getEmailVerified());
        }

        UserEntity updatedAdmin = userRepository.save(admin);
        auditLogger.logAdminUpdated(requestor, updatedAdmin, changes.toString());

        return convertToAdminResponse(updatedAdmin);
    }

    /**
     * Enable/Disable admin
     * SUPER_ADMIN only
     * When disabled, sessions must be invalidated
     */
    public AdminResponse updateAdminStatus(UserEntity requestor, Long adminId, AdminStatusUpdateRequest request) {
        validateAccessToAdminEndpoint(requestor, "update admin status");

        UserEntity admin = findAdminById(adminId);

        if (!permissionChecker.canDisableAdmin(requestor, admin)) {
            throw new UnauthorizedRoleException("You cannot disable this admin");
        }

        boolean isDisabling = Boolean.FALSE.equals(request.getIsActive());

        if (isDisabling) {
            auditLogger.logAdminDisabled(requestor, admin, request.getReason());
            admin.setUserStatus(UserStatus.BLOCKED); // Maps requested active state to entity's UserStatus strategy
            admin.incrementTokenVersion(); // Safe built-in method from your entity
        } else {
            auditLogger.logAdminEnabled(requestor, admin);
            admin.setUserStatus(UserStatus.ACTIVE);
        }

        UserEntity updatedAdmin = userRepository.save(admin);
        return convertToAdminResponse(updatedAdmin);
    }

    /**
     * Reset admin password
     * SUPER_ADMIN only
     */
    public AdminResponse resetAdminPassword(UserEntity requestor, Long adminId, AdminPasswordResetRequest request) {
        validateAccessToAdminEndpoint(requestor, "reset admin password");

        UserEntity admin = findAdminById(adminId);

        if (!permissionChecker.canResetAdminPassword(requestor, admin)) {
            throw new UnauthorizedRoleException("You cannot reset this admin's password");
        }

        // Encode new password
        admin.setPassword(passwordEncoder.encode(request.getNewPassword()));
        admin.incrementTokenVersion(); // Using the secure model tracking method
        admin.setResetOtp(null);
        admin.setResetOtpExpiry(null);

        UserEntity updatedAdmin = userRepository.save(admin);
        auditLogger.logPasswordReset(requestor, updatedAdmin);

        return convertToAdminResponse(updatedAdmin);
    }

    /**
     * Disable admin and invalidate sessions
     */
    public void disableAdminAndInvalidateSessions(UserEntity admin) {
        admin.setUserStatus(UserStatus.BLOCKED);
        admin.incrementTokenVersion();
        admin.setUpdatedAt(LocalDateTime.now());
        userRepository.save(admin);
    }

    /**
     * Helper: Validate requestor has access to admin endpoints
     */
    private void validateAccessToAdminEndpoint(UserEntity requestor, String endpoint) {
        if (requestor == null) {
            throw new UnauthorizedRoleException("Requestor is null");
        }

        if (permissionChecker.isUserAttemptingAdminAccess(requestor)) {
            auditLogger.logUnauthorizedAccess(requestor, "/admin/" + endpoint,
                    "USER attempting admin access");
            throw new UnauthorizedRoleException("Users cannot access admin endpoints");
        }

        if (!roleValidator.hasAdminPrivilege(requestor)) {
            auditLogger.logUnauthorizedAccess(requestor, "/admin/" + endpoint,
                    "Insufficient role: " + requestor.getAdminRole());
            throw new UnauthorizedRoleException("Insufficient privileges for this operation");
        }

        if (!roleValidator.isActiveAndAuthorized(requestor)) {
            throw new UnauthorizedRoleException("Admin account is inactive");
        }
    }

    /**
     * Helper: Find admin by ID
     */
    private UserEntity findAdminById(Long adminId) {
        return userRepository.findById(adminId)
                .orElseThrow(() -> new AdminManagementException("Admin not found with ID: " + adminId));
    }

    /**
     * Helper: Normalize role value
     */
    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return "ROLE_USER";
        }

        String normalized = role.toUpperCase().trim();
        if (!normalized.startsWith("ROLE_")) {
            normalized = "ROLE_" + normalized;
        }

        return normalized;
    }

    /**
     * Helper: Convert UserEntity to AdminResponse
     */
    private AdminResponse convertToAdminResponse(UserEntity admin) {
        return AdminResponse.builder()
                .id(admin.getId())
                .userId(admin.getUserId())
                .email(admin.getEmail())
                .contactPerson(admin.getContactPerson())
                .phoneNumber(admin.getPhoneNumber())
                .role(admin.getRole()) // Utilizes entity helper method to map Enum to String format cleanly
                .address(admin.getAddress())
                .entityName(admin.getEntityName())
                .entityType(admin.getEntityType())
                .emailVerified(admin.getEmailVerified())
                .isActive(admin.isActive()) // Uses entity helper .isActive() evaluation
                .lastLoginAt(admin.getLastLoginAt())
                .createdAt(admin.getCreatedAt())
                .updatedAt(admin.getUpdatedAt())
                .userStatus(admin.getUserStatus() != null ? admin.getUserStatus().toString() : null)
                .build();
    }
}