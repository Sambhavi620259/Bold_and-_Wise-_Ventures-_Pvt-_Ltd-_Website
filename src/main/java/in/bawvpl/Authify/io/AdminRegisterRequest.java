package in.bawvpl.Authify.io;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminRegisterRequest {

    private String name;

    private String email;

    private String password;

    private String phoneNumber;

    private String adminSecret;
}