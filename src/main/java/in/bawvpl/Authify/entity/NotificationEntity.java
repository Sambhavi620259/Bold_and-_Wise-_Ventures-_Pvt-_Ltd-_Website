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
        name = "notifications",

        indexes = {

                @Index(
                        name = "idx_notification_user",
                        columnList = "user_id"
                ),

                @Index(
                        name = "idx_notification_read",
                        columnList = "read_status"
                ),

                @Index(
                        name = "idx_notification_created",
                        columnList = "created_at"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {
        "user"
})
@JsonIgnoreProperties({
        "hibernateLazyInitializer",
        "handler"
})
public class NotificationEntity {

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
    // TITLE
    // =====================================================

    @Column(
            name = "title",
            nullable = false,
            length = 150
    )
    private String title;

    // =====================================================
    // MESSAGE
    // =====================================================

    @Column(
            name = "message",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String message;

    // =====================================================
    // READ STATUS
    // =====================================================

    @Builder.Default
    @Column(
            name = "read_status",
            nullable = false
    )
    private Boolean read = false;

    // =====================================================
    // TYPE
    // INFO / ALERT / SYSTEM / ADMIN
    // =====================================================

    @Builder.Default
    @Column(
            name = "type",
            length = 50
    )
    private String type = "INFO";

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
    // AUTO CREATE
    // =====================================================

    @PrePersist
    protected void onCreate() {

        LocalDateTime now =
                LocalDateTime.now();

        // =====================================================
        // DEFAULTS
        // =====================================================

        if (this.read == null) {

            this.read = false;
        }

        if (

                this.type == null ||

                        this.type.isBlank()
        ) {

            this.type = "INFO";
        }

        // =====================================================
        // NORMALIZE
        // =====================================================

        normalizeFields();

        // =====================================================
        // TIMESTAMPS
        // =====================================================

        if (this.createdAt == null) {

            this.createdAt = now;
        }

        this.updatedAt = now;
    }

    // =====================================================
    // AUTO UPDATE
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

        if (this.title != null) {

            this.title =
                    this.title.trim();
        }

        if (this.message != null) {

            this.message =
                    this.message.trim();
        }

        if (this.type != null) {

            this.type =
                    this.type
                            .trim()
                            .toUpperCase()
                            .replace(" ", "_");
        }
    }

    // =====================================================
    // HELPERS
    // =====================================================

    public boolean isUnread() {

        return !Boolean.TRUE.equals(this.read);
    }

    public boolean isReadNotification() {

        return Boolean.TRUE.equals(this.read);
    }

    public void markAsRead() {

        this.read = true;

        this.updatedAt =
                LocalDateTime.now();
    }

    public void markAsUnread() {

        this.read = false;

        this.updatedAt =
                LocalDateTime.now();
    }

    // =====================================================
    // FRONTEND COMPATIBILITY
    // =====================================================

    public Boolean getIsRead() {

        return this.read;
    }

    public String getNotificationType() {

        return this.type;
    }


}