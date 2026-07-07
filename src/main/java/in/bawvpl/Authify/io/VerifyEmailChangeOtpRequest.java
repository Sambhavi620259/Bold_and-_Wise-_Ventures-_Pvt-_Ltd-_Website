package in.bawvpl.Authify.io;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyEmailChangeOtpRequest {

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "OTP is required")
    private String otp;
}