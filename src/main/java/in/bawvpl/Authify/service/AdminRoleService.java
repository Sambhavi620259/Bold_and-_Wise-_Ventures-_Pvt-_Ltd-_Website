package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.UserEntity;

public interface AdminRoleService {

    void requestRoleChangeOtp(
            UserEntity requestor
    );

    void verifyRoleChangeOtp(
            UserEntity requestor,
            String otp
    );

    void changeRole(
            UserEntity requestor,
            Long targetUserId,
            String newRole
    );
}