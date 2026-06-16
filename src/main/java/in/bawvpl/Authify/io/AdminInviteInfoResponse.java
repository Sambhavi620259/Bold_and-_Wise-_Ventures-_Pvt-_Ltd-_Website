package in.bawvpl.Authify.io;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminInviteInfoResponse {

    private String email;

    private String fullName;

    private String role;

    private boolean expired;

    private boolean used;
}