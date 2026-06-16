package in.bawvpl.Authify.service;

public interface AdminOtpVerificationService {

    void markVerified(
            Long userId,
            String purpose
    );

    boolean isVerified(
            Long userId,
            String purpose
    );

    void consumeVerification(
            Long userId,
            String purpose
    );
}