package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.UserEntity;

import in.bawvpl.Authify.io.ProfileRequest;
import in.bawvpl.Authify.io.ProfileResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface ProfileService {

    // =====================================================
    // PROFILE
    // =====================================================

    ProfileResponse createProfile(
            ProfileRequest request
    );

    ProfileResponse getProfile(
            String email
    );

    // =====================================================
    // PROFILE UPDATE
    // =====================================================

    ProfileResponse updateProfile(

            String email,

            ProfileRequest request
    );

    // =====================================================
    // PROFILE PHOTO
    // =====================================================

    String updateProfilePhoto(

            String email,

            String photoUrl
    );

    // =====================================================
    // USER ID
    // =====================================================

    String getLoggedInUserId(
            String email
    );

    // =====================================================
    // USER
    // =====================================================

    boolean existsByEmail(
            String email
    );

    UserEntity findByEmail(
            String email
    );

    UserEntity save(
            UserEntity userEntity
    );

    // =====================================================
    // EMAIL VERIFICATION
    // =====================================================

    void verifyEmailToken(
            String token
    );

    void sendVerificationOtp(
            String email
    );

    void verifyEmailOtp(

            String email,

            String otp
    );

    // =====================================================
    // RESET PASSWORD
    // =====================================================

    void sendResetOtp(
            String email
    );

    void resetPassword(

            String email,

            String otp,

            String newPassword
    );

    // =====================================================
    // EMAIL CHANGE
    // =====================================================

    void requestEmailChange(

            String currentEmail,

            String newEmail
    );

    void verifyEmailChangeOtp(

            String email,

            String otp
    );

    void resendEmailChangeOtp(
            String email
    );

    // =====================================================
    // PHONE
    // =====================================================

    void sendPhoneOtp(

            String email,

            String phoneNumber
    );

    void verifyPhoneOtp(

            String email,

            String otp
    );

    // =====================================================
    // CONTACT UPDATE
    // =====================================================

    void initiateContactUpdate(

            String email,

            String newEmail,

            String newPhone
    );

    void verifyContactUpdate(

            String email,

            String otp,

            String newEmail,

            String newPhone
    );

    // =====================================================
    // KYC
    // =====================================================

    String getKycRejectionReason(
            String email
    );
    Map<String, Object> reuploadKyc(

            String email,

            String documentType,

            String documentNumber,

            MultipartFile file
    );

    // =====================================================
    // CANONICAL KYC STATUS
    //
    // VERIFIED
    // APPROVED
    // UNDER_REVIEW
    // REUPLOAD_REQUIRED
    // REJECTED
    // PENDING
    // =====================================================

    String getKycStatus(
            String email
    );

    Boolean isKycVerified(
            String email
    );

    // =====================================================
    // LAST LOGIN
    // =====================================================

    String getLastLogin(
            String email
    );

    // =====================================================
    // PROFILE HELPERS
    // =====================================================

    default String normalizePhotoUrl(
            String url
    ) {

        if (

                url == null ||

                        url.isBlank()
        ) {

            return null;
        }

        url = url.trim();

        // =====================================================
        // ALREADY ABSOLUTE
        // =====================================================

        if (

                url.startsWith("http://") ||

                        url.startsWith("https://")
        ) {

            return url;
        }

        // =====================================================
        // ENSURE LEADING SLASH
        // =====================================================

        if (!url.startsWith("/")) {

            url = "/" + url;
        }

        // =====================================================
        // FULL URL
        // =====================================================

        return "http://43.205.116.38:8080" + url;
    }

    // =====================================================
    // OPTIONAL HELPERS
    // =====================================================

    default boolean isEmailVerified(
            UserEntity user
    ) {

        return user != null &&

                Boolean.TRUE.equals(
                        user.getEmailVerified()
                );
    }

    default boolean isPhoneVerified(
            UserEntity user
    ) {

        return user != null &&

                Boolean.TRUE.equals(
                        user.getPhoneVerified()
                );
    }

    default boolean isKycApproved(
            String status
    ) {

        if (

                status == null ||

                        status.isBlank()
        ) {

            return false;
        }

        return status.equalsIgnoreCase("VERIFIED") ||

                status.equalsIgnoreCase("APPROVED");
    }

    // =====================================================
    // PROFILE VALIDATION
    // =====================================================

    default boolean isValidProfile(
            ProfileRequest request
    ) {

        return request != null

                &&

                request.getEmail() != null

                &&

                !request.getEmail().isBlank()

                &&

                request.getName() != null

                &&

                !request.getName().isBlank();
    }

    // =====================================================
    // SAFE STRING
    // =====================================================

    default String safe(
            String value
    ) {

        return value == null

                ? null

                : value.trim();
    }
}