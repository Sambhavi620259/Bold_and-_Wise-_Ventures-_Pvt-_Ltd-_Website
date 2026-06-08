package in.bawvpl.Authify.io;

import in.bawvpl.Authify.entity.AppStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationCreateRequest {

    // =====================================================
    // NAME
    // =====================================================

    @NotBlank(message = "App name is required")
    @Size(
            min = 2,
            max = 150,
            message = "App name must be between 2 and 150 characters"
    )
    private String name;

    // =====================================================
    // SLUG
    // =====================================================

    @NotBlank(message = "Slug is required")
    @Size(
            min = 2,
            max = 150,
            message = "Slug must be between 2 and 150 characters"
    )
    @Pattern(
            regexp = "^[a-z0-9-]+$",
            message = "Slug must contain only lowercase letters, numbers and hyphens"
    )
    private String slug;

    // =====================================================
    // DESCRIPTION
    // =====================================================

    @Size(
            max = 5000,
            message = "Description too long"
    )
    private String description;

    // =====================================================
    // CATEGORY
    // =====================================================

    @Size(
            max = 100,
            message = "Category too long"
    )
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
    // ROUTE PATH
    // =====================================================

    @Size(
            max = 500,
            message = "Route path too long"
    )
    private String routePath;

    // =====================================================
    // EXTERNAL URL
    // =====================================================

    @Size(
            max = 1000,
            message = "External URL too long"
    )
    private String externalUrl;

    // =====================================================
    // APP URL
    // =====================================================

    @Size(
            max = 1000,
            message = "App URL too long"
    )
    private String appUrl;

    // =====================================================
    // LOGO
    // =====================================================

    @Size(
            max = 1000,
            message = "Logo URL too long"
    )
    private String logoUrl;

    // =====================================================
    // BANNER
    // =====================================================

    @Size(
            max = 1000,
            message = "Banner URL too long"
    )
    private String bannerUrl;

    // =====================================================
    // VERSION
    // =====================================================

    @Size(
            max = 50,
            message = "Version too long"
    )
    private String version;

    // =====================================================
    // VALIDATION
    // =====================================================

    public boolean hasValidNavigationTarget() {

        return

                hasText(routePath)

                        ||

                        hasText(externalUrl)

                        ||

                        hasText(appUrl);
    }

    // =====================================================
    // NORMALIZATION
    // =====================================================

    public void normalize() {

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

        // =====================================================
        // NAME
        // =====================================================

        if (this.name != null) {

            this.name =
                    this.name.trim();
        }

        // =====================================================
        // SLUG
        // =====================================================

        if (this.slug != null) {

            this.slug =
                    this.slug
                            .trim()
                            .toLowerCase()

                            // replace spaces
                            .replace(" ", "-")

                            // collapse double hyphens
                            .replace("--", "-");
        }

        // =====================================================
        // DESCRIPTION
        // =====================================================

        if (this.description != null) {

            this.description =
                    this.description.trim();
        }

        // =====================================================
        // CATEGORY
        // =====================================================

        if (this.category != null) {

            this.category =
                    this.category
                            .trim()
                            .toUpperCase();
        }

        // =====================================================
        // VISIBILITY
        // =====================================================

        if (this.visibility != null) {

            this.visibility =
                    this.visibility
                            .trim()
                            .toUpperCase();
        }

        // =====================================================
        // ROUTE PATH
        // =====================================================

        if (this.routePath != null) {

            this.routePath =
                    this.routePath.trim();

            if (

                    !this.routePath.isBlank() &&

                            !this.routePath.startsWith("/")
            ) {

                this.routePath =
                        "/" + this.routePath;
            }
        }

        // =====================================================
        // URLS
        // =====================================================

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

        // =====================================================
        // VERSION
        // =====================================================

        if (this.version != null) {

            this.version =
                    this.version.trim();
        }
    }

    // =====================================================
    // HELPERS
    // =====================================================

    private boolean hasText(
            String value
    ) {

        return value != null &&

                !value.isBlank();
    }

    // =====================================================
    // STATUS HELPERS
    // =====================================================

    public boolean isDraft() {

        return this.status ==
                AppStatus.DRAFT;
    }

    public boolean isPublished() {

        return this.status ==
                AppStatus.PUBLISHED;
    }

    public boolean isSuspended() {

        return this.status ==
                AppStatus.SUSPENDED;
    }

    // =====================================================
    // VISIBILITY HELPERS
    // =====================================================

    public boolean isPublic() {

        return "PUBLIC"
                .equalsIgnoreCase(
                        this.visibility
                );
    }

    public boolean isPrivate() {

        return "PRIVATE"
                .equalsIgnoreCase(
                        this.visibility
                );
    }
}