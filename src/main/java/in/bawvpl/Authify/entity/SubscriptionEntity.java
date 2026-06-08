package in.bawvpl.Authify.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "subscriptions",

        indexes = {

                @Index(
                        name = "idx_subscription_user",
                        columnList = "user_id"
                ),

                @Index(
                        name = "idx_subscription_status",
                        columnList = "status"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {
        "user",
        "plan"
})
@JsonIgnoreProperties({
        "hibernateLazyInitializer",
        "handler"
})
public class SubscriptionEntity {

    // =====================================================
    // ID
    // =====================================================

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;

    // =====================================================
    // STATUS
    // ACTIVE / EXPIRED / CANCELLED
    // =====================================================

    @Builder.Default
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    private String status = "ACTIVE";

    // =====================================================
    // START DATE
    // =====================================================

    @Column(name = "start_date")
    private LocalDateTime startDate;

    // =====================================================
    // END DATE
    // =====================================================

    @Column(name = "end_date")
    private LocalDateTime endDate;

    // =====================================================
    // USER
    // =====================================================

    @ManyToOne(
            fetch = FetchType.LAZY
    )
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private UserEntity user;

    // =====================================================
    // PLAN
    // =====================================================

    @ManyToOne(
            fetch = FetchType.LAZY
    )
    @JoinColumn(name = "plan_id")
    @JsonIgnoreProperties({
            "hibernateLazyInitializer",
            "handler"
    })
    private SubscriptionPlan plan;

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
    // UPDATED AT
    // =====================================================

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // =====================================================
    // PRE PERSIST
    // =====================================================

    @PrePersist
    protected void onCreate() {

        LocalDateTime now =
                LocalDateTime.now();

        if (this.createdAt == null) {

            this.createdAt = now;
        }

        this.updatedAt = now;

        if (

                this.status == null ||

                        this.status.isBlank()
        ) {

            this.status = "ACTIVE";
        }

        if (this.startDate == null) {

            this.startDate = now;
        }

        normalizeFields();
    }

    // =====================================================
    // PRE UPDATE
    // =====================================================

    @PreUpdate
    protected void onUpdate() {

        this.updatedAt =
                LocalDateTime.now();

        normalizeFields();
    }

    // =====================================================
    // NORMALIZATION
    // =====================================================

    private void normalizeFields() {

        if (this.status != null) {

            this.status =
                    this.status
                            .trim()
                            .toUpperCase();
        }
    }

    // =====================================================
    // HELPERS
    // =====================================================

    public boolean isActive() {

        return "ACTIVE"
                .equalsIgnoreCase(
                        this.status
                );
    }

    public boolean isExpired() {

        return "EXPIRED"
                .equalsIgnoreCase(
                        this.status
                );
    }

    public boolean isCancelled() {

        return "CANCELLED"
                .equalsIgnoreCase(
                        this.status
                );
    }
}