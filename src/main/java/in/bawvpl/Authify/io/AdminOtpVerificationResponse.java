package in.bawvpl.Authify.io;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminOtpVerificationResponse {

    private String actionToken;

    private Long expiresIn;

}
