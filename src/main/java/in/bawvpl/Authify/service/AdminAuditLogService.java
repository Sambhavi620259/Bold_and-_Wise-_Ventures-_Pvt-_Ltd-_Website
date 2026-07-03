package in.bawvpl.Authify.service;

public interface AdminAuditLogService {

    void logRoleChange(
            Long performedBy,
            Long targetUserId,
            String oldRole,
            String newRole
    );

    void logInviteCreated(
            Long performedBy,
            String email,
            String role
    );

    void logEmailChange(
            Long performedBy,
            Long targetUserId,
            String oldEmail,
            String newEmail
    );

    void logPhoneChange(
            Long performedBy,
            Long targetUserId,
            String oldPhone,
            String newPhone
    );
}