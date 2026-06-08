package in.bawvpl.Authify.service;

public interface SmsService {

    // 🔹 Generic sender (returns status)
    boolean sendSms(String phoneNumber, String message);

    // 🔹 OTP helpers (return delivery status)
    boolean sendVerificationOtp(String phoneNumber, String otp);

    boolean sendResetOtp(String phoneNumber, String otp);
}