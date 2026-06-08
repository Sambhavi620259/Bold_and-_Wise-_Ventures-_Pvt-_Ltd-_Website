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
        name = "applications",

        indexes = {

                @Index(
                        name = "idx_app_user",
                        columnList = "user_id"
                ),

                @Index(
                        name = "idx_app_status",
                        columnList = "status"
                ),

                @Index(
                        name = "idx_app_visibility",
                        columnList = "visibility"
                ),

                @Index(
                        name = "idx_app_slug",
                        columnList = "slug"
                ),

                @Index(
                        name = "idx_app_featured",
                        columnList = "featured"
                ),

                @Index(
                        name = "idx_app_reserved",
                        columnList = "reserved_flag"
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
public class ApplicationEntity {

    // =====================================================
    // BASE URL
    // =====================================================

    private static final String BASE_URL =
            "http://43.205.116.38:8080";

    // =====================================================
    // ID
    // =====================================================

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    @Column(name = "app_id")
    private Long appId;

    // =====================================================
    // OWNER
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
    // BASIC DETAILS
    // =====================================================

    @Column(
            name = "name",
            nullable = false,
            length = 150
    )
    private String name;

    @Column(
            name = "slug",
            nullable = false,
            unique = true,
            length = 150
    )
    private String slug;

    @Column(
            name = "description",
            columnDefinition = "TEXT"
    )
    private String description;

    @Column(
            name = "category",
            length = 100
    )
    private String category;

    // =====================================================
    // STATUS
    // =====================================================

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    private AppStatus status =
            AppStatus.DRAFT;

    // =====================================================
    // VISIBILITY
    // =====================================================

    @Builder.Default
    @Column(
            name = "visibility",
            nullable = false,
            length = 30
    )
    private String visibility =
            "PUBLIC";

    // =====================================================
    // FEATURED
    // =====================================================

    @Builder.Default
    @Column(
            name = "featured",
            nullable = false
    )
    private Boolean featured =
            false;

    // =====================================================
    // RESERVED FLAG
    // =====================================================

    @Builder.Default
    @Column(
            name = "reserved_flag",
            nullable = false
    )
    private Boolean reserved =
            false;

    // =====================================================
    // ROUTING
    // =====================================================

    @Column(
            name = "route_path",
            length = 500
    )
    private String routePath;

    @Column(
            name = "external_url",
            length = 1000
    )
    private String externalUrl;

    @Column(
            name = "app_url",
            length = 1000
    )
    private String appUrl;

    // =====================================================
    // ASSETS
    // =====================================================

    @Column(
            name = "logo_url",
            length = 1000
    )
    private String logoUrl;

    @Column(
            name = "banner_url",
            length = 1000
    )
    private String bannerUrl;

    // =====================================================
    // FRONTEND FALLBACK IMAGE FIELDS
    // =====================================================

    @Transient
    private String appLogo;

    @Transient
    private String imageUrl;

    @Transient
    private String iconUrl;

    // =====================================================
    // VERSION
    // =====================================================

    @Column(
            name = "version",
            length = 50
    )
    private String version;

    // =====================================================
    // ANALYTICS
    // =====================================================

    @Builder.Default
    @Column(
            name = "downloads",
            nullable = false
    )
    private Long downloads =
            0L;

    @Builder.Default
    @Column(
            name = "active_users",
            nullable = false
    )
    private Long activeUsers =
            0L;

    // =====================================================
    // CREATED BY
    // =====================================================

    @Column(
            name = "created_by",
            length = 150
    )
    private String createdBy;

    // =====================================================
    // TIMESTAMPS
    // =====================================================

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // =====================================================
    // AUTO CREATE
    // =====================================================

    @PrePersist
    protected void onCreate() {

        LocalDateTime now =
                LocalDateTime.now();

        if (this.status == null) {

            this.status =
                    AppStatus.DRAFT;
        }

        if (

                this.visibility == null ||

                        this.visibility.isBlank()
        ) {

            this.visibility =
                    "PUBLIC";
        }

        if (this.featured == null) {

            this.featured = false;
        }

        if (this.reserved == null) {

            this.reserved = false;
        }

        if (this.downloads == null) {

            this.downloads = 0L;
        }

        if (this.activeUsers == null) {

            this.activeUsers = 0L;
        }

        normalizeFields();

        syncImageFields();

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

        syncImageFields();
    }

    // =====================================================
    // NORMALIZATION
    // =====================================================

    private void normalizeFields() {

        if (this.name != null) {

            this.name =
                    this.name.trim();
        }

        if (this.slug != null) {

            this.slug =
                    this.slug
                            .trim()
                            .toLowerCase();
        }

        if (this.description != null) {

            this.description =
                    this.description.trim();
        }

        if (this.category != null) {

            this.category =
                    this.category
                            .trim()
                            .toUpperCase();
        }

        if (this.visibility != null) {

            this.visibility =
                    this.visibility
                            .trim()
                            .toUpperCase();
        }

        if (

                this.routePath != null &&

                        !this.routePath.isBlank()
        ) {

            this.routePath =
                    this.routePath.trim();

            if (!this.routePath.startsWith("/")) {

                this.routePath =
                        "/" + this.routePath;
            }
        }

        if (this.externalUrl != null) {

            this.externalUrl =
                    this.externalUrl.trim();
        }

        if (this.appUrl != null) {

            this.appUrl =
                    this.appUrl.trim();
        }

        if (this.logoUrl != null) {

            this.logoUrl =
                    this.logoUrl.trim();
        }

        if (this.bannerUrl != null) {

            this.bannerUrl =
                    this.bannerUrl.trim();
        }

        if (this.version != null) {

            this.version =
                    this.version.trim();
        }

        if (this.createdBy != null) {

            this.createdBy =
                    this.createdBy.trim();
        }
    }

    // =====================================================
    // IMAGE FALLBACKS
    // =====================================================

    private void syncImageFields() {

        String resolvedImage = null;

        if (

                this.logoUrl != null &&

                        !this.logoUrl.isBlank()
        ) {

            resolvedImage =
                    resolveUrl(this.logoUrl);

        } else if (

                this.bannerUrl != null &&

                        !this.bannerUrl.isBlank()
        ) {

            resolvedImage =
                    resolveUrl(this.bannerUrl);
        }

        if (

                resolvedImage != null &&

                        !resolvedImage.isBlank()
        ) {

            this.logoUrl =
                    resolvedImage;

            this.appLogo =
                    resolvedImage;

            this.imageUrl =
                    resolvedImage;

            this.iconUrl =
                    resolvedImage;
        }
    }

    // =====================================================
    // URL RESOLVER
    // =====================================================

    private String resolveUrl(
            String value
    ) {

        if (

                value == null ||

                        value.isBlank()
        ) {

            return null;
        }

        value = value.trim();

        if (

                value.startsWith("http://") ||

                        value.startsWith("https://")
        ) {

            return value;
        }

        if (!value.startsWith("/")) {

            value = "/" + value;
        }

        return BASE_URL + value;
    }

    // =====================================================
    // HELPERS
    // =====================================================

    public boolean isPublished() {

        return this.status ==
                AppStatus.PUBLISHED;
    }

    public boolean isArchived() {

        return this.status ==
                AppStatus.ARCHIVED;
    }

    public boolean isSuspended() {

        return this.status ==
                AppStatus.SUSPENDED;
    }

    public boolean isDraft() {

        return this.status ==
                AppStatus.DRAFT;
    }

    public boolean isPublic() {

        return "PUBLIC"
                .equalsIgnoreCase(
                        this.visibility
                );
    }

    public boolean isFeatured() {

        return Boolean.TRUE.equals(
                this.featured
        );
    }

    public boolean isReserved() {

        return Boolean.TRUE.equals(
                this.reserved
        );
    }

    // =====================================================
    // FRONTEND SAFE IMAGE
    // =====================================================

    public String getResolvedImage() {

        if (

                this.logoUrl != null &&

                        !this.logoUrl.isBlank()
        ) {

            return this.logoUrl;
        }

        if (

                this.bannerUrl != null &&

                        !this.bannerUrl.isBlank()
        ) {

            return this.bannerUrl;
        }

        return null;
    }
}