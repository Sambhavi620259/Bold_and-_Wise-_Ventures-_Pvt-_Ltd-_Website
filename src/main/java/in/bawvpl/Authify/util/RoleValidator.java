package in.bawvpl.Authify.util;

import in.bawvpl.Authify.entity.AdminRole;
import in.bawvpl.Authify.entity.UserEntity;
import in.bawvpl.Authify.entity.UserStatus;
import org.springframework.stereotype.Component;

@Component
public class RoleValidator {

    public boolean isOwner(UserEntity user) {
        return user != null
                && user.getAdminRole() == AdminRole.ROLE_OWNER;
    }

    public boolean isAdmin(UserEntity user) {
        return user != null
                && user.getAdminRole() == AdminRole.ROLE_ADMIN;
    }

    public boolean isAdminOrOwner(UserEntity user) {
        return isAdmin(user) || isOwner(user);
    }

    public boolean isUser(UserEntity user) {
        return user == null
                || user.getAdminRole() == AdminRole.ROLE_USER;
    }

    public boolean canManageAdmins(UserEntity user) {
        return isOwner(user);
    }

    public boolean hasAdminPrivilege(UserEntity user) {
        return isAdmin(user) || isOwner(user);
    }

    public boolean isActiveAndAuthorized(UserEntity user) {
        return user != null
                && user.getUserStatus() == UserStatus.ACTIVE
                && hasAdminPrivilege(user);
    }

    // Compatibility methods for old code

    public boolean isSuperAdmin(UserEntity user) {
        return isOwner(user);
    }

    public boolean isAdminOrSuperAdmin(UserEntity user) {
        return isAdminOrOwner(user);
    }

    public boolean canAssignRole(UserEntity requestor, String newRole) {

        if (!isOwner(requestor)) {
            return false;
        }

        return newRole != null
                && !newRole.isBlank();
    }
}