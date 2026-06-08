package in.bawvpl.Authify.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

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
        name = "user_applications",

        uniqueConstraints = {

                @UniqueConstraint(
                        name = "uk_user_app",
                        columnNames = {
                                "user_id",
                                "app_id"
                        }
                )
        },

        indexes = {

                @Index(
                        name = "idx_user_app",
                        columnList = "user_id, app_id"
                ),

                @Index(
                        name = "idx_user_subscription_status",
                        columnList = "subscription_status"
                ),

                @Index(
                        name = "idx_user_active",
                        columnList = "is_active"
                ),

                @Index(
                        name = "idx_user_last_opened",
                        columnList = "last_opened_at"
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
        "app"
})
@JsonIgnoreProperties({
        "hibernateLazyInitializer",
        "handler"
})
public class UserApplicationEntity {

    // =====================================================
    // ID
    // =====================================================

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;

    // =====================================================
    // USER
    // =====================================================

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    @JsonIgnore
    private UserEntity user;

    // =====================================================
    // APPLICATION
    // =====================================================

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "app_id",
            nullable = false
    )
    @JsonIgnoreProperties({
            "hibernateLazyInitializer",
            "handler"
    })
    private ApplicationEntity app;

    // =====================================================
    // VISIT COUNT
    // =====================================================

    @Builder.Default
    @JsonProperty("visitCounter")
    @Column(
            name = "visit_counter",
            nullable = false
    )
    private Integer visitCounter = 0;

    // =====================================================
    // LAST OPENED
    //
    // IMPORTANT:
    //
    // Used for:
    // - recent apps
    // - analytics
    // - usage graph
    // - app activity timeline
    //
    // MUST update ONLY on real app open.
    // =====================================================

    @Column(
            name = "last_opened_at"
    )
    private LocalDateTime lastOpenedAt;

    // =====================================================
    // SUBSCRIPTION STATUS
    // =====================================================

    @Builder.Default
    @Column(
            name = "subscription_status",
            nullable = false,
            length = 30
    )
    private String subscriptionStatus = "APPLIED";

    // =====================================================
    // ACTIVE FLAG
    // =====================================================

    @Builder.Default
    @Column(
            name = "is_active",
            nullable = false
    )
    private Boolean active = true;

    // =====================================================
    // TIMESTAMPS
    // =====================================================

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @Column(
            name = "updated_at"
    )
    private LocalDateTime updatedAt;

    // =====================================================
    // AUTO CREATE
    // =====================================================

    @PrePersist
    protected void onCreate() {

        LocalDateTime now =
                LocalDateTime.now();

        applyDefaults();

        normalizeFields();

        if (createdAt == null) {

            createdAt = now;
        }

        updatedAt = now;
    }

    // =====================================================
    // AUTO UPDATE
    // =====================================================

    @PreUpdate
    protected void onUpdate() {

        applyDefaults();

        normalizeFields();

        updatedAt =
                LocalDateTime.now();
    }

    // =====================================================
    // DEFAULTS
    // =====================================================

    private void applyDefaults() {

        if (visitCounter == null) {

            visitCounter = 0;
        }

        if (

                subscriptionStatus == null ||

                        subscriptionStatus.isBlank()
        ) {

            subscriptionStatus = "APPLIED";
        }

        if (active == null) {

            active = true;
        }
    }

    // =====================================================
    // NORMALIZE
    // =====================================================

    private void normalizeFields() {

        if (subscriptionStatus != null) {

            subscriptionStatus =
                    subscriptionStatus
                            .trim()
                            .toUpperCase();
        }
    }

    // =====================================================
    // APP OPEN TRACKING
    //
    // IMPORTANT:
    //
    // ONLY call this on:
    // - actual app launch/open
    //
    // DO NOT call on:
    // - subscribe
    // - install
    // - apply
    // =====================================================

    public void trackOpen() {

        this.lastOpenedAt =
                LocalDateTime.now();

        if (this.visitCounter == null) {

            this.visitCounter = 0;
        }

        this.visitCounter =
                this.visitCounter + 1;
    }

    // =====================================================
    // HELPERS
    // =====================================================

    public boolean isApplied() {

        return "APPLIED"
                .equalsIgnoreCase(
                        this.subscriptionStatus
                );
    }

    public boolean isActiveSubscription() {

        return "ACTIVE"
                .equalsIgnoreCase(
                        this.subscriptionStatus
                );
    }

    public boolean isExpired() {

        return "EXPIRED"
                .equalsIgnoreCase(
                        this.subscriptionStatus
                );
    }

    public boolean isCancelled() {

        return "CANCELLED"
                .equalsIgnoreCase(
                        this.subscriptionStatus
                );
    }

    public boolean isBlocked() {

        return "BLOCKED"
                .equalsIgnoreCase(
                        this.subscriptionStatus
                );
    }

    // =====================================================
    // SAFE ACCESSORS
    // =====================================================

    public Long getUserId() {

        return this.user != null

                ? this.user.getId()

                : null;
    }

    public Long getAppId() {

        return this.app != null

                ? this.app.getAppId()

                : null;
    }

    public String getAppName() {

        return this.app != null

                ? this.app.getName()

                : null;
    }

    public String getAppLogo() {

        return this.app != null

                ? this.app.getLogoUrl()

                : null;
    }

    public String getAppRoutePath() {

        return this.app != null

                ? this.app.getRoutePath()

                : null;
    }
}