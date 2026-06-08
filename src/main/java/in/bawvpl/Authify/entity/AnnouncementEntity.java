package in.bawvpl.Authify.entity;

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
        name = "announcements",

        indexes = {

                @Index(
                        name = "idx_announcement_published",
                        columnList = "published"
                ),

                @Index(
                        name = "idx_announcement_created",
                        columnList = "created_at"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@JsonIgnoreProperties({
        "hibernateLazyInitializer",
        "handler"
})
public class AnnouncementEntity {

    // =====================================================
    // ID
    // =====================================================

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;

    // =====================================================
    // TITLE
    // =====================================================

    @Column(
            nullable = false,
            length = 255
    )
    private String title;

    // =====================================================
    // MESSAGE
    // =====================================================

    @Column(
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String message;

    // =====================================================
    // BANNER URL
    // =====================================================

    @Column(
            name = "banner_url",
            length = 500
    )
    private String bannerUrl;

    // =====================================================
    // PUBLISHED
    // =====================================================

    @Builder.Default
    @Column(
            nullable = false
    )
    private Boolean published = true;

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

        if (this.createdAt == null) {

            this.createdAt = now;
        }

        this.updatedAt = now;

        normalize();
    }

    // =====================================================
    // AUTO UPDATE
    // =====================================================

    @PreUpdate
    protected void onUpdate() {

        this.updatedAt =
                LocalDateTime.now();

        normalize();
    }

    // =====================================================
    // NORMALIZATION
    // =====================================================

    private void normalize() {

        if (this.title != null) {

            this.title =
                    this.title.trim();
        }

        if (this.message != null) {

            this.message =
                    this.message.trim();
        }

        if (this.bannerUrl != null) {

            this.bannerUrl =
                    this.bannerUrl.trim();
        }

        if (this.published == null) {

            this.published = true;
        }
    }

    // =====================================================
    // FRONTEND HELPERS
    // =====================================================

    public boolean isPublished() {

        return Boolean.TRUE.equals(
                this.published
        );
    }
}