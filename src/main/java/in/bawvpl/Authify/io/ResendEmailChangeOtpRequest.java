package in.bawvpl.Authify.io;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResendEmailChangeOtpRequest {

    @NotBlank(message = "User ID is required")
    private String userId;
}