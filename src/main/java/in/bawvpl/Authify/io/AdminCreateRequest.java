package in.bawvpl.Authify.io;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new admin user
 * SUPER_ADMIN only operation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminCreateRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 100, message = "First name must be between 2 and 100 characters")
    private String contactPerson;

    @Size(max = 20, message = "Phone number must be at most 20 characters")
    private String phoneNumber;

    @NotBlank(message = "Role is required")
    @JsonProperty("role")
    private String role;  // ROLE_ADMIN (SUPER_ADMIN cannot be created from API)

    @Size(max = 500, message = "Address must be at most 500 characters")
    private String address;

    @NotNull(message = "Email verification status must be specified")
    private Boolean emailVerified;

    private String entityName;

    private String entityType;
}

