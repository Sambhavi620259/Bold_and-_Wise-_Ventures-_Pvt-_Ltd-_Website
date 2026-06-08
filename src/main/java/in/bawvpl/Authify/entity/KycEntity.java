package in.bawvpl.Authify.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "kyc",
        indexes = {

                @Index(
                        name = "idx_kyc_user",
                        columnList = "user_id"
                ),

                @Index(
                        name = "idx_kyc_status",
                        columnList = "status"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({
        "hibernateLazyInitializer",
        "handler"
})
public class KycEntity {

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
    private Long id;

    // =====================================================
    // USER
    // =====================================================

    @ManyToOne(
            fetch = FetchType.LAZY
    )
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    @JsonIgnore
    private UserEntity user;

    // =====================================================
    // DOCUMENT
    // =====================================================

    @Column(
            name = "document_type",
            nullable = false,
            length = 50
    )
    private String documentType;

    @Column(
            name = "document_number",
            nullable = false,
            length = 100
    )
    private String documentNumber;

    // =====================================================
    // PRIMARY FILE
    // =====================================================

    @Column(
            name = "file_path",
            length = 1000
    )
    private String filePath;

    // =====================================================
    // DOCUMENT URLS
    // =====================================================

    @Column(name = "aadhaar_front_url", length = 1000)
    private String aadhaarFrontUrl;

    @Column(name = "aadhaar_back_url", length = 1000)
    private String aadhaarBackUrl;

    @Column(name = "pan_card_url", length = 1000)
    private String panCardUrl;

    @Column(name = "passport_url", length = 1000)
    private String passportUrl;

    @Column(name = "driving_license_url", length = 1000)
    private String drivingLicenseUrl;

    @Column(name = "selfie_url", length = 1000)
    private String selfieUrl;

    @Column(name = "live_photo_url", length = 1000)
    private String livePhotoUrl;

    // =====================================================
    // FALLBACK URLS
    // =====================================================

    @Column(name = "front_document_url", length = 1000)
    private String frontDocumentUrl;

    @Column(name = "back_document_url", length = 1000)
    private String backDocumentUrl;

    // =====================================================
    // META
    // =====================================================

    @Column(name = "mime_type", length = 150)
    private String mimeType;

    @Column(name = "content_type", length = 150)
    private String contentType;

    // =====================================================
    // STATUS
    // =====================================================

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(
            name = "status",
            nullable = false,
            length = 50
    )
    private KycStatus status =
            KycStatus.PENDING;

    @Builder.Default
    @Column(
            name = "completed",
            nullable = false
    )
    private Boolean completed =
            false;

    // =====================================================
    // REVIEW
    // =====================================================

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(
            name = "reviewed_by",
            length = 150
    )
    private String reviewedBy;

    // =====================================================
    // VERIFIED
    // =====================================================

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(
            name = "verified_by",
            length = 150
    )
    private String verifiedBy;

    // =====================================================
    // REJECTION
    // =====================================================

    @Column(
            name = "rejection_reason",
            length = 1000
    )
    private String rejectionReason;

    // =====================================================
    // FLAGS
    // =====================================================

    @Builder.Default
    @Column(
            name = "risk_flag",
            nullable = false
    )
    private Boolean riskFlag =
            false;

    // =====================================================
    // TIMESTAMPS
    // =====================================================

    @Column(
            name = "uploaded_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime uploadedAt;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // =====================================================
    // CREATE
    // =====================================================

    @PrePersist
    protected void onCreate() {

        LocalDateTime now =
                LocalDateTime.now();

        if (this.uploadedAt == null) {

            this.uploadedAt = now;
        }

        if (this.submittedAt == null) {

            this.submittedAt = now;
        }

        this.updatedAt = now;

        if (this.status == null) {

            this.status =
                    KycStatus.PENDING;
        }

        if (this.completed == null) {

            this.completed = false;
        }

        if (this.riskFlag == null) {

            this.riskFlag = false;
        }

        normalizeFields();

        syncDocumentUrls();
    }

    // =====================================================
    // UPDATE
    // =====================================================

    @PreUpdate
    protected void onUpdate() {

        this.updatedAt =
                LocalDateTime.now();

        normalizeFields();

        syncDocumentUrls();
    }

    // =====================================================
    // NORMALIZE
    // =====================================================

    private void normalizeFields() {

        if (this.documentType != null) {

            this.documentType =
                    this.documentType
                            .trim()
                            .toUpperCase();
        }

        if (this.documentNumber != null) {

            this.documentNumber =
                    this.documentNumber
                            .trim()
                            .toUpperCase();
        }

        if (this.rejectionReason != null) {

            this.rejectionReason =
                    this.rejectionReason.trim();
        }

        if (this.reviewedBy != null) {

            this.reviewedBy =
                    this.reviewedBy.trim();
        }

        if (this.verifiedBy != null) {

            this.verifiedBy =
                    this.verifiedBy.trim();
        }

        if (this.mimeType != null) {

            this.mimeType =
                    this.mimeType.trim();
        }

        if (this.contentType != null) {

            this.contentType =
                    this.contentType.trim();
        }

        if (this.status == null) {

            this.status =
                    KycStatus.PENDING;
        }

        if (this.completed == null) {

            this.completed = false;
        }

        if (this.riskFlag == null) {

            this.riskFlag = false;
        }
    }

    // =====================================================
    // URL FALLBACKS
    // =====================================================

    private void syncDocumentUrls() {

        if (

                this.filePath != null &&

                        !this.filePath.isBlank()
        ) {

            this.filePath =
                    resolveUrl(this.filePath);

            if (

                    this.frontDocumentUrl == null ||

                            this.frontDocumentUrl.isBlank()
            ) {

                this.frontDocumentUrl =
                        this.filePath;
            }

            if (

                    this.backDocumentUrl == null ||

                            this.backDocumentUrl.isBlank()
            ) {

                this.backDocumentUrl =
                        this.filePath;
            }
        }

        this.frontDocumentUrl =
                resolveUrl(this.frontDocumentUrl);

        this.backDocumentUrl =
                resolveUrl(this.backDocumentUrl);

        this.aadhaarFrontUrl =
                resolveUrl(this.aadhaarFrontUrl);

        this.aadhaarBackUrl =
                resolveUrl(this.aadhaarBackUrl);

        this.panCardUrl =
                resolveUrl(this.panCardUrl);

        this.passportUrl =
                resolveUrl(this.passportUrl);

        this.drivingLicenseUrl =
                resolveUrl(this.drivingLicenseUrl);

        this.selfieUrl =
                resolveUrl(this.selfieUrl);

        this.livePhotoUrl =
                resolveUrl(this.livePhotoUrl);

        // =====================================================
        // FALLBACKS
        // =====================================================

        if (

                (this.aadhaarFrontUrl == null ||

                        this.aadhaarFrontUrl.isBlank()) &&

                        this.frontDocumentUrl != null
        ) {

            this.aadhaarFrontUrl =
                    this.frontDocumentUrl;
        }

        if (

                (this.aadhaarBackUrl == null ||

                        this.aadhaarBackUrl.isBlank()) &&

                        this.backDocumentUrl != null
        ) {

            this.aadhaarBackUrl =
                    this.backDocumentUrl;
        }

        if (

                "PAN".equalsIgnoreCase(this.documentType) &&

                        (this.panCardUrl == null ||

                                this.panCardUrl.isBlank()) &&

                        this.frontDocumentUrl != null
        ) {

            this.panCardUrl =
                    this.frontDocumentUrl;
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

    public boolean isVerified() {

        return this.status ==
                KycStatus.VERIFIED;
    }

    public boolean isPending() {

        return this.status ==
                KycStatus.PENDING;
    }

    public boolean isRejected() {

        return this.status ==
                KycStatus.REJECTED;
    }

    public boolean isUnderReview() {

        return this.status ==
                KycStatus.UNDER_REVIEW;
    }

    public boolean isReuploadRequired() {

        return this.status ==
                KycStatus.REUPLOAD_REQUIRED;
    }

    // =====================================================
    // FIXED:
    // APPROVED SUPPORT
    // =====================================================

    public boolean isApproved() {

        return this.status ==
                KycStatus.VERIFIED ||

                this.status ==
                        KycStatus.APPROVED;
    }
}