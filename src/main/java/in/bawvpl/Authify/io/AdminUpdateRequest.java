package in.bawvpl.Authify.io;

import lombok.Data;

@Data
public class AdminUpdateRequest {

    private String entityName;

    private String role;

    private String contactPerson;

    private String phoneNumber;

    private String address;

    private Boolean emailVerified;
}