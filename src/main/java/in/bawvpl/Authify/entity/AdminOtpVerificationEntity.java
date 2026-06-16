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
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 50)
    private String purpose;

    @Builder.Default
    @Column(nullable = false)
    private Boolean verified = false;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime verifiedAt =
            LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime expiresAt;
}