package in.bawvpl.Authify.io;

import lombok.Data;

@Data
public class AdminStatusUpdateRequest {

    private Boolean isActive;

    private String reason;
}