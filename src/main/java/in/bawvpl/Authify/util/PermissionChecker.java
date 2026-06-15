package in.bawvpl.Authify.util;

import in.bawvpl.Authify.entity.UserEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Central permission checking utility
 * All authorization must be checked server-side
 */
@Slf4j
@Component
public class PermissionChecker {

    private final RoleValidator roleValidator;

    public PermissionChecker(RoleValidator roleValidator) {
        this.roleValidator = roleValidator;
    }

    /**
     * Check if user can create admin
     */
    public boolean canCreateAdmin(UserEntity requestor) {
        boolean authorized = roleValidator.canManageAdmins(requestor);
        if (!authorized) {
            log.warn("Unauthorized admin creation attempt by: {}",
                    requestor != null ? requestor.getEmail() : "null");
        }
        return authorized;
    }

    /**
     * Check if user can update admin
     */
    public boolean canUpdateAdmin(UserEntity requestor, UserEntity target) {
        if (!roleValidator.canManageAdmins(requestor)) {
            log.warn("Unauthorized admin update attempt by: {} on: {}",
                    requestor.getEmail(), target.getEmail());
            return false;
        }

        // Cannot modify self unless absolutely necessary (password reset allowed)
        // PREVENT: ADMIN from modifying another ADMIN
        if (roleValidator.isAdmin(requestor) && roleValidator.isAdmin(target)) {
            log.error("SECURITY: ADMIN {} attempted to modify ADMIN {}",
                    requestor.getEmail(), target.getEmail());
            return false;
        }

        return true;
    }

    /**
     * Check if user can disable admin
     */
    public boolean canDisableAdmin(UserEntity requestor, UserEntity target) {
        if (!roleValidator.canManageAdmins(requestor)) {
            log.warn("Unauthorized admin disable attempt by: {} on: {}",
                    requestor.getEmail(), target != null ? target.getEmail() : "null");
            return false;
        }

        // Cannot disable self
        if (requestor.getId().equals(target.getId())) {
            log.warn("Self-disable attempt by admin: {}", requestor.getEmail());
            return false;
        }

        return true;
    }

    /**
     * Check if user can reset admin password
     */
    public boolean canResetAdminPassword(UserEntity requestor, UserEntity target) {
        if (!roleValidator.canManageAdmins(requestor)) {
            log.warn("Unauthorized password reset attempt by: {} on: {}",
                    requestor.getEmail(), target.getEmail());
            return false;
        }

        // SUPER_ADMIN can reset anyone's password
        // ADMIN can only reset USER passwords (not ADMIN/SUPER_ADMIN passwords
        if (roleValidator.isAdmin(requestor)) {
            if (roleValidator.isAdminOrSuperAdmin(target)) {
                log.error("SECURITY: ADMIN {} attempted to reset password for {} ({})",
                        requestor.getEmail(), target.getEmail(), target.getAdminRole());
                return false;
            }
        }

        return true;
    }

    /**
     * Check if user can list admins
     */
    public boolean canListAdmins(UserEntity requestor) {
        boolean authorized = roleValidator.hasAdminPrivilege(requestor) &&
                roleValidator.isActiveAndAuthorized(requestor);
        if (!authorized) {
            log.warn("Unauthorized admin list attempt by: {}",
                    requestor != null ? requestor.getEmail() : "null");
        }
        return authorized;
    }

    /**
     * Check if user can view admin details
     */
    public boolean canViewAdminDetails(UserEntity requestor, UserEntity target) {
        if (!roleValidator.hasAdminPrivilege(requestor)) {
            log.warn("Unauthorized admin view attempt by: {} on: {}",
                    requestor.getEmail(), target.getEmail());
            return false;
        }

        // ADMIN can only view other ADMINs (not SUPER_ADMIN details)
        if (roleValidator.isAdmin(requestor) && roleValidator.isSuperAdmin(target)) {
            log.warn("ADMIN {} attempted to view SUPER_ADMIN {}",
                    requestor.getEmail(), target.getEmail());
            return false;
        }

        return true;
    }

    /**
     * CRITICAL: Prevent USER from accessing admin APIs
     */
    public boolean isUserAttemptingAdminAccess(UserEntity user) {
        if (user == null) {
            return false;
        }

        boolean isUser = roleValidator.isUser(user);
        if (isUser) {
            log.error("SECURITY BREACH ATTEMPT: USER {} attempted admin access", user.getEmail());
        }
        return isUser;
    }

    /**
     * Check if role change is allowed
     */
    public boolean canChangeRole(UserEntity requestor, UserEntity target, String newRole) {
        if (!roleValidator.canAssignRole(requestor, newRole)) {
            return false;
        }

        // Cannot downgrades SUPER_ADMIN
        if (roleValidator.isSuperAdmin(target) && !roleValidator.isSuperAdmin(requestor)) {
            log.error("SECURITY: {} attempted to downgrade SUPER_ADMIN {}",
                    requestor.getEmail(), target.getEmail());
            return false;
        }

        return true;
    }
}

