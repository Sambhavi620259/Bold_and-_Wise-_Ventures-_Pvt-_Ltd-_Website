package in.bawvpl.Authify.io;

import lombok.Data;

@Data
public class UpdateContactRequest {
    private String newEmail;
    private String newPhone;
}
