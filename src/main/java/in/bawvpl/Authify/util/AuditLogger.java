package in.bawvpl.Authify.util;

import in.bawvpl.Authify.entity.UserEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Centralized audit logging for admin operations
 */
@Slf4j
@Component
public class AuditLogger {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

    /**
     * Log admin creation
     */
    public void logAdminCreated(UserEntity createdBy, UserEntity newAdmin) {
        log.info("AUDIT [ADMIN_CREATED]: Created by={} ({}), New admin={} ({}), Email={}, Role={}, Timestamp={}",
                createdBy.getId(),
                createdBy.getEmail(),
                newAdmin.getId(),
                newAdmin.getUserId(),
                newAdmin.getEmail(),
                newAdmin.getAdminRole(),
                LocalDateTime.now().format(formatter));
    }

    /**
     * Log admin update
     */
    public void logAdminUpdated(UserEntity updatedBy, UserEntity admin, String changes) {
        log.info("AUDIT [ADMIN_UPDATED]: Updated by={} ({}), Admin={} ({}), Email={}, Changes={}, Timestamp={}",
                updatedBy.getId(),
                updatedBy.getEmail(),
                admin.getId(),
                admin.getUserId(),
                admin.getEmail(),
                changes,
                LocalDateTime.now().format(formatter));
    }

    /**
     * Log admin disable
     */
    public void logAdminDisabled(UserEntity disabledBy, UserEntity admin, String reason) {
        log.info("AUDIT [ADMIN_DISABLED]: Disabled by={} ({}), Admin={} ({}), Email={}, Reason={}, Timestamp={}",
                disabledBy.getId(),
                disabledBy.getEmail(),
                admin.getId(),
                admin.getUserId(),
                admin.getEmail(),
                reason != null ? reason : "Not specified",
                LocalDateTime.now().format(formatter));
    }

    /**
     * Log admin enable
     */
    public void logAdminEnabled(UserEntity enabledBy, UserEntity admin) {
        log.info("AUDIT [ADMIN_ENABLED]: Enabled by={} ({}), Admin={} ({}), Email={}, Timestamp={}",
                enabledBy.getId(),
                enabledBy.getEmail(),
                admin.getId(),
                admin.getUserId(),
                admin.getEmail(),
                LocalDateTime.now().format(formatter));
    }

    /**
     * Log password reset
     */
    public void logPasswordReset(UserEntity resetBy, UserEntity admin) {
        log.info("AUDIT [PASSWORD_RESET]: Reset by={} ({}), Admin={} ({}), Email={}, Timestamp={}",
                resetBy.getId(),
                resetBy.getEmail(),
                admin.getId(),
                admin.getUserId(),
                admin.getEmail(),
                LocalDateTime.now().format(formatter));
    }

    /**
     * Log role change
     */
    public void logRoleChanged(UserEntity changedBy, UserEntity admin, String oldRole, String newRole) {
        log.info("AUDIT [ROLE_CHANGED]: Changed by={} ({}), Admin={} ({}), Email={}, Old role={}, New role={}, Timestamp={}",
                changedBy.getId(),
                changedBy.getEmail(),
                admin.getId(),
                admin.getUserId(),
                admin.getEmail(),
                oldRole,
                newRole,
                LocalDateTime.now().format(formatter));
    }

    /**
     * Log suspicious activity
     */
    public void logSuspiciousActivity(String activityType, UserEntity user, String details) {
        log.error("SECURITY [SUSPICIOUS_ACTIVITY]: Type={}, User={} ({}), Email={}, Details={}, Timestamp={}",
                activityType,
                user != null ? user.getId() : "unknown",
                user != null ? user.getUserId() : "unknown",
                user != null ? user.getEmail() : "unknown",
                details,
                LocalDateTime.now().format(formatter));
    }

    /**
     * Log unauthorized access attempt
     */
    public void logUnauthorizedAccess(UserEntity user, String endpoint, String reason) {
        log.error("SECURITY [UNAUTHORIZED_ACCESS]: User={} ({}), Email={}, Endpoint={}, Reason={}, Timestamp={}",
                user != null ? user.getId() : "unknown",
                user != null ? user.getUserId() : "unknown",
                user != null ? user.getEmail() : "unknown",
                endpoint,
                reason,
                LocalDateTime.now().format(formatter));
    }

    /**
     * Log privilege escalation attempt
     */
    public void logPrivilegeEscalationAttempt(UserEntity user, String attemptedAction) {
        log.error("SECURITY [PRIVILEGE_ESCALATION_ATTEMPT]: User={} ({}), Email={}, Role={}, Action={}, Timestamp={}",
                user.getId(),
                user.getUserId(),
                user.getEmail(),
                user.getAdminRole(),
                attemptedAction,
                LocalDateTime.now().format(formatter));
    }

    /**
     * Log admin login
     */
    public void logAdminLogin(UserEntity admin) {
        log.info("AUDIT [ADMIN_LOGIN]: Admin={} ({}), Email={}, Role={}, IP=?, Timestamp={}",
                admin.getId(),
                admin.getUserId(),
                admin.getEmail(),
                admin.getAdminRole(),
                LocalDateTime.now().format(formatter));
    }

    /**
     * Log admin logout
     */
    public void logAdminLogout(UserEntity admin) {
        log.info("AUDIT [ADMIN_LOGOUT]: Admin={} ({}), Email={}, Role={}, Timestamp={}",
                admin.getId(),
                admin.getUserId(),
                admin.getEmail(),
                admin.getAdminRole(),
                LocalDateTime.now().format(formatter));
    }
}

