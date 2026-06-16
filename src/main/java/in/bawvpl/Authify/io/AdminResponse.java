package in.bawvpl.Authify.io;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminResponse {

    private Long id;

    private String userId;

    private Long entityId;

    private String email;

    private String contactPerson;

    private String phoneNumber;

    private String role;

    private String address;

    private String entityName;

    private String entityType;

    private Boolean emailVerified;

    private Boolean isActive;

    private LocalDateTime lastLoginAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String userStatus;
}