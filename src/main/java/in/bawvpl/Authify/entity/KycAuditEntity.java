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
        name = "kyc_audit_logs",
        indexes = {

                @Index(
                        name = "idx_kyc_audit_kyc",
                        columnList = "kyc_id"
                ),

                @Index(
                        name = "idx_kyc_audit_action",
                        columnList = "action"
                ),

                @Index(
                        name = "idx_kyc_audit_created",
                        columnList = "created_at"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KycAuditEntity {

    // =====================================================
    // ID
    // =====================================================

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;

    // =====================================================
    // KYC RELATION
    // =====================================================

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "kyc_id",
            nullable = false
    )
    private KycEntity kyc;

    // =====================================================
    // ACTION
    // EXAMPLES:
    // SUBMITTED
    // VERIFIED
    // REJECTED
    // UNDER_REVIEW
    // REUPLOAD_REQUIRED
    // =====================================================

    @Column(
            nullable = false,
            length = 50
    )
    private String action;

    // =====================================================
    // OLD STATUS
    // =====================================================

    @Column(
            name = "old_status",
            length = 50
    )
    private String oldStatus;

    // =====================================================
    // NEW STATUS
    // =====================================================

    @Column(
            name = "new_status",
            length = 50
    )
    private String newStatus;

    // =====================================================
    // MODERATION NOTE
    // =====================================================

    @Column(
            columnDefinition = "TEXT"
    )
    private String note;

    // =====================================================
    // REJECTION REASON
    // =====================================================

    @Column(
            name = "rejection_reason",
            columnDefinition = "TEXT"
    )
    private String rejectionReason;

    // =====================================================
    // REVIEWED BY
    // =====================================================

    @Column(
            name = "reviewed_by",
            length = 150
    )
    private String reviewedBy;

    // =====================================================
    // IP ADDRESS
    // OPTIONAL
    // =====================================================

    @Column(
            name = "ip_address",
            length = 100
    )
    private String ipAddress;

    // =====================================================
    // USER AGENT
    // OPTIONAL
    // =====================================================

    @Column(
            name = "user_agent",
            columnDefinition = "TEXT"
    )
    private String userAgent;

    // =====================================================
    // CREATED AT
    // =====================================================

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    // =====================================================
    // AUTO CREATE
    // =====================================================

    @PrePersist
    protected void onCreate() {

        if (this.createdAt == null) {

            this.createdAt =
                    LocalDateTime.now();
        }
    }
}