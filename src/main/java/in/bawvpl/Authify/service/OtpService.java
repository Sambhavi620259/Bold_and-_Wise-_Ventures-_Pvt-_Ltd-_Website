package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.UserEntity;

public interface OtpService {

    // =====================================================
    // LOGIN OTP
    // =====================================================

    String generateLoginOtp(
            UserEntity user
    );

    void verifyLoginOtp(

            UserEntity user,

            String otp
    );

    // =====================================================
    // REGISTER OTP
    // =====================================================

    String generateRegisterOtp(
            UserEntity user
    );

    void verifyRegisterOtp(

            UserEntity user,

            String otp
    );

    // =====================================================
    // RESET PASSWORD OTP
    // =====================================================

    String generateResetOtp(
            UserEntity user
    );

    void verifyResetOtp(

            UserEntity user,

            String otp
    );

    // =====================================================
    // PHONE OTP
    // =====================================================

    String generatePhoneOtp(
            UserEntity user
    );

    void verifyPhoneOtp(

            UserEntity user,

            String otp
    );

    // =====================================================
    // GENERIC RANDOM OTP
    // =====================================================

    String generateOtp();
}