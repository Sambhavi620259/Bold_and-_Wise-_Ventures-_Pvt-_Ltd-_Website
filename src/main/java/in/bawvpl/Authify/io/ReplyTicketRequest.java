package in.bawvpl.Authify.io;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReplyTicketRequest {

    // =====================================================
    // MESSAGE
    // =====================================================

    @NotBlank(message = "Message is required")
    @Size(
            min = 1,
            max = 5000,
            message = "Message must be between 1 and 5000 characters"
    )
    private String message;
}