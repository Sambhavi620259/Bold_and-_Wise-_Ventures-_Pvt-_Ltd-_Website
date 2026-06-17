package in.bawvpl.Authify.service;

public interface AdminOtpVerificationService {

    void markVerified(
            Long userId,
            String purpose,
            String actionToken
    );

    boolean isVerified(
            Long userId,
            String purpose
    );

    void consumeVerification(
            Long userId,
            String purpose
    );

    void validateActionToken(
            Long userId,
            String actionToken,
            String purpose
    );

    void consumeActionToken(
            String actionToken
    );

}