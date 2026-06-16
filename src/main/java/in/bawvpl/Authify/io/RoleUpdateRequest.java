package in.bawvpl.Authify.io;

import lombok.Data;

@Data
public class RoleUpdateRequest {

    /**
     * USER
     * ADMIN
     * OWNER
     */
    private String role;
}