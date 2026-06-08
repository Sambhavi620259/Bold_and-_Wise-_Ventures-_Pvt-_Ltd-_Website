package in.bawvpl.Authify.io;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserHistoryResponse {

    private String userId;

    private String oldEmail;
    private String newEmail;

    private String oldPhone;
    private String newPhone;

    private String changedBy;

    private LocalDateTime changedAt;
}