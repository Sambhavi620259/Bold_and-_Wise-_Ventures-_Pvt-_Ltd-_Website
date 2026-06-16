package in.bawvpl.Authify.entity;

import jakarta.persistence.*;

import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "admin_audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminAuditLogEntity {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;

    @Column(name = "action", nullable = false)
    private String action;

    @Column(name = "performed_by", nullable = false)
    private Long performedBy;

    @Column(name = "target_user_id")
    private Long targetUserId;

    @Column(name = "old_role")
    private String oldRole;

    @Column(name = "new_role")
    private String newRole;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt =
            LocalDateTime.now();
}