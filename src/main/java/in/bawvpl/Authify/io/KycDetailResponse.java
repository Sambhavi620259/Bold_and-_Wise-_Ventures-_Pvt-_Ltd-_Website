package in.bawvpl.Authify.io;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KycDetailResponse {

    // =====================================================
    // BASE URL
    // =====================================================

    private static final String BASE_URL =
            "http://43.205.116.38:8080";

    // =====================================================
    // KYC ID
    // =====================================================

    private Long id;

    // =====================================================
    // USER INFO
    // =====================================================

    private Long userId;

    private String fullName;

    private String applicantName;

    private String name;

    private String email;

    private String phone;

    private String phoneNumber;

    // =====================================================
    // PERSONAL DETAILS
    // =====================================================

    private String dateOfBirth;

    private String address;

    private String country;

    // =====================================================
    // KYC STATUS
    // =====================================================

    private String kycStatus;

    private String status;

    // =====================================================
    // DOCUMENT DETAILS
    // =====================================================

    private String documentType;

    private String documentNumber;

    // =====================================================
    // DOCUMENT URLS
    // =====================================================

    private String aadhaarFrontUrl;

    private String aadhaarBackUrl;

    private String panCardUrl;

    private String passportUrl;

    private String drivingLicenseUrl;

    // =====================================================
    // SELFIE / LIVE PHOTO
    // =====================================================

    private String selfieUrl;

    private String livePhotoUrl;

    // =====================================================
    // GENERIC FALLBACK URLS
    // =====================================================

    private String frontDocumentUrl;

    private String backDocumentUrl;

    private String documentFront;

    private String documentBack;

    private String documentUrl;

    private String documentFile;

    // =====================================================
    // FILE METADATA
    // =====================================================

    private String mimeType;

    private String contentType;

    // =====================================================
    // TIMESTAMPS
    // =====================================================

    private LocalDateTime uploadedAt;

    private LocalDateTime submittedAt;

    private LocalDateTime reviewedAt;

    private LocalDateTime verifiedAt;

    // =====================================================
    // REVIEW INFO
    // =====================================================

    private String reviewedBy;

    private String verifiedBy;

    private String rejectionReason;

    // =====================================================
    // FLAGS
    // =====================================================

    private Boolean riskFlag;

    private Boolean completed;

    // =====================================================
    // NORMALIZATION
    // =====================================================

    public void normalize() {

        // =====================================================
        // NAME FALLBACKS
        // =====================================================

        if (

                (this.fullName == null ||

                        this.fullName.isBlank()) &&

                        this.name != null
        ) {

            this.fullName =
                    this.name;
        }

        if (

                (this.name == null ||

                        this.name.isBlank()) &&

                        this.fullName != null
        ) {

            this.name =
                    this.fullName;
        }

        if (

                (this.applicantName == null ||

                        this.applicantName.isBlank()) &&

                        this.fullName != null
        ) {

            this.applicantName =
                    this.fullName;
        }

        // =====================================================
        // PHONE FALLBACKS
        // =====================================================

        if (

                (this.phone == null ||

                        this.phone.isBlank()) &&

                        this.phoneNumber != null
        ) {

            this.phone =
                    this.phoneNumber;
        }

        if (

                (this.phoneNumber == null ||

                        this.phoneNumber.isBlank()) &&

                        this.phone != null
        ) {

            this.phoneNumber =
                    this.phone;
        }

        // =====================================================
        // STATUS FALLBACKS
        // =====================================================

        if (

                (this.kycStatus == null ||

                        this.kycStatus.isBlank()) &&

                        this.status != null
        ) {

            this.kycStatus =
                    this.status;
        }

        if (

                (this.status == null ||

                        this.status.isBlank()) &&

                        this.kycStatus != null
        ) {

            this.status =
                    this.kycStatus;
        }

        if (

                this.status == null ||

                        this.status.isBlank()
        ) {

            this.status = "PENDING";

            this.kycStatus = "PENDING";
        }

        this.status =
                this.status
                        .trim()
                        .toUpperCase();

        this.kycStatus =
                this.status;

        // =====================================================
        // URL NORMALIZATION
        // =====================================================

        this.frontDocumentUrl =
                normalizeUrl(this.frontDocumentUrl);

        this.backDocumentUrl =
                normalizeUrl(this.backDocumentUrl);

        this.aadhaarFrontUrl =
                normalizeUrl(this.aadhaarFrontUrl);

        this.aadhaarBackUrl =
                normalizeUrl(this.aadhaarBackUrl);

        this.panCardUrl =
                normalizeUrl(this.panCardUrl);

        this.passportUrl =
                normalizeUrl(this.passportUrl);

        this.drivingLicenseUrl =
                normalizeUrl(this.drivingLicenseUrl);

        this.selfieUrl =
                normalizeUrl(this.selfieUrl);

        this.livePhotoUrl =
                normalizeUrl(this.livePhotoUrl);

        this.documentUrl =
                normalizeUrl(this.documentUrl);

        this.documentFile =
                normalizeUrl(this.documentFile);

        // =====================================================
        // FALLBACKS
        // =====================================================

        if (

                (this.frontDocumentUrl == null ||

                        this.frontDocumentUrl.isBlank()) &&

                        this.documentUrl != null
        ) {

            this.frontDocumentUrl =
                    this.documentUrl;
        }

        if (

                (this.backDocumentUrl == null ||

                        this.backDocumentUrl.isBlank()) &&

                        this.documentUrl != null
        ) {

            this.backDocumentUrl =
                    this.documentUrl;
        }

        if (

                (this.documentFront == null ||

                        this.documentFront.isBlank()) &&

                        this.frontDocumentUrl != null
        ) {

            this.documentFront =
                    this.frontDocumentUrl;
        }

        if (

                (this.documentBack == null ||

                        this.documentBack.isBlank()) &&

                        this.backDocumentUrl != null
        ) {

            this.documentBack =
                    this.backDocumentUrl;
        }

        if (

                (this.documentUrl == null ||

                        this.documentUrl.isBlank()) &&

                        this.frontDocumentUrl != null
        ) {

            this.documentUrl =
                    this.frontDocumentUrl;
        }

        if (

                (this.documentFile == null ||

                        this.documentFile.isBlank()) &&

                        this.documentUrl != null
        ) {

            this.documentFile =
                    this.documentUrl;
        }

        // =====================================================
        // DEFAULT FLAGS
        // =====================================================

        if (this.completed == null) {

            this.completed = false;
        }

        if (this.riskFlag == null) {

            this.riskFlag = false;
        }
    }

    // =====================================================
    // URL NORMALIZER
    // =====================================================

    private String normalizeUrl(
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
}