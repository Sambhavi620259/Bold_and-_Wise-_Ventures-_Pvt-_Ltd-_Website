package in.bawvpl.Authify.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_sessions",
        indexes = {
                @Index(name = "idx_session_user", columnList = "user_id"),
                @Index(name = "idx_session_token", columnList = "token")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ================= USER =================
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // ================= TOKEN =================
    @Column(nullable = false, length = 1000) // 🔥 increased (JWT can be long)
    private String token;

    // ================= DEVICE INFO =================
    @Column(name = "ip_address", length = 50)
    private String ip;

    @Column(length = 300) // 🔥 increased for long user-agent
    private String device;

    // ================= STATUS =================
    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    // ================= TIMESTAMP =================
    @Column(name = "login_time", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ================= AUTO =================
    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (active == null) {
            active = true;
        }
    }
}