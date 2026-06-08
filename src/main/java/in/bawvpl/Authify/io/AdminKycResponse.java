package in.bawvpl.Authify.io;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminKycResponse {

    // =====================================================
    // PRIMARY
    // =====================================================

    private Long id;

    // =====================================================
    // USER
    // =====================================================

    private String userId;

    // =====================================================
    // CANONICAL FRONTEND FIELDS
    // =====================================================

    private String applicantName;

    private String kycStatus;

    // =====================================================
    // FALLBACK USER FIELDS
    // =====================================================

    private String name;

    private String email;

    private String phoneNumber;

    private String address;

    // =====================================================
    // APPLICANT
    // =====================================================

    private Applicant applicant;

    // =====================================================
    // USER FALLBACK
    // =====================================================

    private Applicant user;

    // =====================================================
    // KYC
    // =====================================================

    private String documentType;

    private String documentNumber;

    private String status;

    private Boolean completed;

    private String rejectionReason;

    // =====================================================
    // FIXED TIMESTAMPS
    // =====================================================

    private LocalDateTime uploadedAt;

    private LocalDateTime verifiedAt;

    private LocalDateTime reviewedAt;

    private LocalDateTime submittedAt;

    private String verifiedBy;

    private String reviewedBy;

    // =====================================================
    // DOCUMENT URLS
    // =====================================================

    private String frontDocumentUrl;

    private String backDocumentUrl;

    private String selfieUrl;

    // =====================================================
    // NEW FRONTEND PREVIEW FIELDS
    // =====================================================

    private String imageUrl;

    private String previewUrl;

    private String filePath;

    // =====================================================
    // FALLBACK DOCUMENT FIELDS
    // =====================================================

    private String documentUrl;

    private String documentFile;

    private String aadhaarFrontUrl;

    private String aadhaarBackUrl;

    private String panCardUrl;

    private String passportUrl;

    private String drivingLicenseUrl;

    private String livePhotoUrl;

    // =====================================================
    // MIME
    // =====================================================

    private String mimeType;

    private String contentType;

    // =====================================================
    // FLAGS
    // =====================================================

    private Boolean riskFlag;

    // =====================================================
    // APPLICANT DTO
    // =====================================================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Applicant {

        private String userId;

        // =====================================================
        // CANONICAL
        // =====================================================

        private String fullName;

        // =====================================================
        // FALLBACKS
        // =====================================================

        private String name;

        private String applicantName;

        private String email;

        private String phoneNumber;

        private String mobile;

        private String address;

        private String profilePhotoUrl;
    }
}