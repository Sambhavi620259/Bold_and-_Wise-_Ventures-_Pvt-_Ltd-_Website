package in.bawvpl.Authify.io;

import lombok.Data;

@Data
public class AdminInviteCompleteRequest {

    private String token;

    private String password;

    private String phoneNumber;
}