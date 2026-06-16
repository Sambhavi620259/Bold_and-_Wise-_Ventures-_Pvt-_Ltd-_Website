package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.UserEntity;
import in.bawvpl.Authify.io.AdminInviteRequest;
import in.bawvpl.Authify.io.AdminInviteInfoResponse;
import in.bawvpl.Authify.io.AdminInviteCompleteRequest;

public interface AdminInviteService {

    void requestInviteOtp(
            UserEntity inviter
    );

    void verifyInviteOtp(
            UserEntity inviter,
            String otp
    );

    void createInvite(
            UserEntity inviter,
            AdminInviteRequest request
    );

    AdminInviteInfoResponse getInviteInfo(
            String token
    );

    void completeInvite(
            AdminInviteCompleteRequest request
    );
}