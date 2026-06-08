package in.bawvpl.Authify.entity;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "audit_logs",

        indexes = {

                @Index(
                        name = "idx_audit_user",
                        columnList = "user_id"
                ),

                @Index(
                        name = "idx_audit_action",
                        columnList = "action"
                ),

                @Index(
                        name = "idx_audit_time",
                        columnList = "created_at"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    // =====================================================
    // PRIMARY KEY
    // =====================================================

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;

    // =====================================================
    // USER
    // =====================================================

    @Column(
            name = "user_id",
            nullable = false
    )
    private Long userId;

    // =====================================================
    // ACTION
    // =====================================================

    @Column(
            nullable = false,
            length = 100
    )
    private String action;

    // =====================================================
    // METADATA / DESCRIPTION
    // =====================================================

    @Column(
            columnDefinition = "TEXT"
    )
    private String metadata;

    // =====================================================
    // CLIENT IP
    // =====================================================

    @Column(
            length = 100
    )
    private String ip;

    // =====================================================
    // USER DEVICE / USER AGENT
    // =====================================================

    @Column(
            columnDefinition = "TEXT"
    )
    private String device;

    // =====================================================
    // CREATED TIME
    // =====================================================

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    // =====================================================
    // AUTO TIMESTAMP
    // =====================================================

    @PrePersist
    public void prePersist() {

        if (createdAt == null) {

            createdAt = LocalDateTime.now();
        }
    }
}