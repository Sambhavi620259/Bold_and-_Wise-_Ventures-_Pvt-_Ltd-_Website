package in.bawvpl.Authify.io;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminInviteResponse {

    private String email;

    private String fullName;

    private String role;

    private boolean used;

    private boolean expired;
}