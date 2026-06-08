package in.bawvpl.Authify.io;

import com.fasterxml.jackson.annotation.JsonInclude;

import in.bawvpl.Authify.entity.AppStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApplicationResponse {

    // =====================================================
    // BASE URL
    // =====================================================

    private static final String BASE_URL =
            "http://43.205.116.38:8080";

    // =====================================================
    // ID
    // =====================================================

    private Long appId;

    // =====================================================
    // BASIC DETAILS
    // =====================================================

    private String name;

    // =====================================================
    // FRONTEND ALIAS
    // =====================================================

    private String appName;

    private String slug;

    private String description;

    // =====================================================
    // FRONTEND ALIAS
    // =====================================================

    private String appText;

    private String category;

    // =====================================================
    // STATUS
    // =====================================================

    @Builder.Default
    private AppStatus status =
            AppStatus.DRAFT;

    // =====================================================
    // VISIBILITY
    // =====================================================

    @Builder.Default
    private String visibility =
            "PUBLIC";

    // =====================================================
    // FEATURED
    // =====================================================

    @Builder.Default
    private Boolean featured =
            false;

    // =====================================================
    // RESERVED
    // =====================================================

    @Builder.Default
    private Boolean reserved =
            false;

    // =====================================================
    // ROUTING
    // =====================================================

    private String routePath;

    private String externalUrl;

    private String appUrl;

    // =====================================================
    // ASSETS
    // =====================================================

    private String logoUrl;

    private String bannerUrl;

    // =====================================================
    // FRONTEND IMAGE ALIASES
    // =====================================================

    private String appLogo;

    private String imageUrl;

    private String iconUrl;

    // =====================================================
    // VERSION
    // =====================================================

    private String version;

    // =====================================================
    // ANALYTICS
    // =====================================================

    @Builder.Default
    private Long downloads =
            0L;

    @Builder.Default
    private Long activeUsers =
            0L;

    // =====================================================
    // CREATED BY
    // =====================================================

    private String createdBy;

    // =====================================================
    // TIMESTAMPS
    // =====================================================

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // =====================================================
    // NORMALIZATION
    // =====================================================

    public void normalize() {

        try {

            // =====================================================
            // DEFAULTS
            // =====================================================

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

            // =====================================================
            // APP NAME ALIAS
            // =====================================================

            if (

                    (this.appName == null ||

                            this.appName.isBlank())

                            &&

                            this.name != null
            ) {

                this.appName =
                        this.name;
            }

            // =====================================================
            // APP TEXT ALIAS
            // =====================================================

            if (

                    (this.appText == null ||

                            this.appText.isBlank())

                            &&

                            this.description != null
            ) {

                this.appText =
                        this.description;
            }

            // =====================================================
            // TRIM FIELDS
            // =====================================================

            if (this.name != null) {

                this.name =
                        safeTrim(this.name);
            }

            if (this.slug != null) {

                this.slug =
                        safeTrim(this.slug)
                                .toLowerCase();
            }

            if (this.description != null) {

                this.description =
                        safeTrim(this.description);
            }

            if (this.category != null) {

                this.category =
                        safeTrim(this.category);
            }

            if (this.version != null) {

                this.version =
                        safeTrim(this.version);
            }

            if (this.createdBy != null) {

                this.createdBy =
                        safeTrim(this.createdBy);
            }

            // =====================================================
            // LOGO URL NORMALIZATION
            // =====================================================

            if (

                    this.logoUrl != null &&

                            !this.logoUrl.isBlank()
            ) {

                this.logoUrl =
                        normalizeUrl(
                                this.logoUrl
                        );
            }

            // =====================================================
            // BANNER URL NORMALIZATION
            // =====================================================

            if (

                    this.bannerUrl != null &&

                            !this.bannerUrl.isBlank()
            ) {

                this.bannerUrl =
                        normalizeUrl(
                                this.bannerUrl
                        );
            }

            // =====================================================
            // FALLBACK IMAGE
            // =====================================================

            String resolvedImage = null;

            if (

                    this.logoUrl != null &&

                            !this.logoUrl.isBlank()
            ) {

                resolvedImage =
                        this.logoUrl;

            } else if (

                    this.bannerUrl != null &&

                            !this.bannerUrl.isBlank()
            ) {

                resolvedImage =
                        this.bannerUrl;
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

            // =====================================================
            // ROUTE PATH
            // =====================================================

            if (

                    this.routePath != null &&

                            !this.routePath.isBlank()
            ) {

                this.routePath =
                        safeTrim(this.routePath);

                if (!this.routePath.startsWith("/")) {

                    this.routePath =
                            "/" + this.routePath;
                }
            }

            // =====================================================
            // URL TRIM
            // =====================================================

            if (this.externalUrl != null) {

                this.externalUrl =
                        safeTrim(this.externalUrl);
            }

            if (this.appUrl != null) {

                this.appUrl =
                        safeTrim(this.appUrl);
            }

        } catch (Exception e) {

            // prevent HTTP 500 during normalization
            e.printStackTrace();
        }
    }

    // =====================================================
    // URL NORMALIZER
    // =====================================================

    private String normalizeUrl(
            String url
    ) {

        try {

            if (

                    url == null ||

                            url.isBlank()
            ) {

                return null;
            }

            url = safeTrim(url);

            if (

                    url.startsWith("http://") ||

                            url.startsWith("https://")
            ) {

                return url;
            }

            if (!url.startsWith("/")) {

                url = "/" + url;
            }

            return BASE_URL + url;

        } catch (Exception e) {

            e.printStackTrace();

            return null;
        }
    }

    // =====================================================
    // SAFE TRIM
    // =====================================================

    private String safeTrim(
            String value
    ) {

        try {

            return value != null
                    ? value.trim()
                    : null;

        } catch (Exception e) {

            return value;
        }
    }

    // =====================================================
    // USER USAGE
    // =====================================================

    @Builder.Default
    private Integer visitCounter = 0;

    // =====================================================
    // HELPERS
    // =====================================================

    public boolean isPublished() {

        return this.status ==
                AppStatus.PUBLISHED;
    }

    public boolean isDraft() {

        return this.status ==
                AppStatus.DRAFT;
    }

    public boolean isSuspended() {

        return this.status ==
                AppStatus.SUSPENDED;
    }

    public boolean isArchived() {

        return this.status ==
                AppStatus.ARCHIVED;
    }

    public boolean isPublic() {

        return "PUBLIC"
                .equalsIgnoreCase(
                        this.visibility
                );
    }

    public boolean isFeaturedApp() {

        return Boolean.TRUE.equals(
                this.featured
        );
    }

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