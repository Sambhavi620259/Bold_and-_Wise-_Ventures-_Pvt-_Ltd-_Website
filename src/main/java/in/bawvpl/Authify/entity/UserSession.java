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
    private String ipAddress;

    @Column(name = "device_name", length = 300)
    private String deviceName;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    // ================= STATUS =================
    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    // ================= TIMESTAMP =================
    @Column(name = "login_time", nullable = false, updatable = false)
    private LocalDateTime loginTime;

    @Column(name = "last_access_time")
    private LocalDateTime lastAccessTime;
    // ================= AUTO =================
    @PrePersist
    public void prePersist() {

        if (loginTime == null) {
            loginTime = LocalDateTime.now();
        }

        if (lastAccessTime == null) {
            lastAccessTime = LocalDateTime.now();
        }

        if (active == null) {
            active = true;
        }
    }
}