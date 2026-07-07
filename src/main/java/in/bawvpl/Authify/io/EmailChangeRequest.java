package in.bawvpl.Authify.io;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmailChangeRequest {

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "New email is required")
    @Email(message = "Invalid email")
    private String newEmail;
}