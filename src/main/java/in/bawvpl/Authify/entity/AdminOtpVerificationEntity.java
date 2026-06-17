package in.bawvpl.Authify.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "admin_otp_verifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminOtpVerificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "purpose", nullable = false, length = 64)
    private String purpose;

    @Builder.Default
    @Column(name = "verified", nullable = false)
    private Boolean verified = false;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "action_token", unique = true, length = 100)
    private String actionToken;

    @Builder.Default
    @Column(name = "consumed", nullable = false)
    private Boolean consumed = false;

    @Column(name = "consumed_at")
    private LocalDateTime consumedAt;

    @PrePersist
    public void prePersist() {

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (verified == null) {
            verified = false;
        }

        if (consumed == null) {
            consumed = false;
        }
    }
}