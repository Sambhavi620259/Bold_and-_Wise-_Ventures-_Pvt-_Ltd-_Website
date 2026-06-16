package in.bawvpl.Authify.io;

import lombok.Data;

@Data
public class AdminInviteRequest {

    private String email;

    private String fullName;

    /**
     * ADMIN
     * OWNER
     */
    private String role;
}