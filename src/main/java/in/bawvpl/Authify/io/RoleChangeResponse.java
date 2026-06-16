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
public class RoleChangeResponse {

    private Long targetUserId;

    private String targetEmail;

    private String oldRole;

    private String newRole;

    private String changedBy;

    private LocalDateTime changedAt;
}