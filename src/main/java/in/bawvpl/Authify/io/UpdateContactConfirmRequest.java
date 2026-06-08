package in.bawvpl.Authify.io;

import lombok.Data;

@Data
public class UpdateContactConfirmRequest {
    private String newEmail;
    private String newPhone;

    private String oldEmailOtp;
    private String newEmailOtp;
    private String newPhoneOtp;
}
