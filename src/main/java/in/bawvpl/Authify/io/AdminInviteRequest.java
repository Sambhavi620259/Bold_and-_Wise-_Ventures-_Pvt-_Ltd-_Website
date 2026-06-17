package in.bawvpl.Authify.io;

import lombok.Data;

@Data
public class AdminInviteRequest {

    private String email;

    private String fullName;

    private String role;

    private String inviteActionToken;
}