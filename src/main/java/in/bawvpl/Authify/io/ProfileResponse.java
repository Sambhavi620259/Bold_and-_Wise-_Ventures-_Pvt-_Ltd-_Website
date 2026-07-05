package in.bawvpl.Authify.io;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProfileResponse {

    // =====================================================
    // USER
    // =====================================================

    private String userId;

    private String name;

    private String fullName;

    private String email;

    private String phoneNumber;

    // =====================================================
    // ROLE
    // =====================================================

    private String role;

    // =====================================================
    // ENTITY TYPE
    // =====================================================

    private String entityType;

    // =====================================================
    // VERIFICATION
    // =====================================================

    private Boolean emailVerified;

    private Boolean phoneVerified;

    private Boolean accountVerified;

    private Boolean kycVerified;

    // =====================================================
    // KYC STATUS
    // =====================================================

    private String kycStatus;

    // =====================================================
    // EXTRA
    // =====================================================

    private String referralCode;

    // =====================================================
    // PROFILE PHOTO
    // =====================================================

    private String photoUrl;

    private String profilePhotoUrl;

    private String avatarUrl;

    // =====================================================
    // FRONTEND FALLBACKS
    // =====================================================

    private String mobile;

    private String phone;

    private String applicantName;

    // =====================================================
    // KYC
    // =====================================================

    private Kyc kyc;

    private String companyName;

    private String designation;

    private String address;

    private String city;

    private String state;

    private String country;

    private String postalCode;

    // =====================================================
    // NORMALIZATION
    // =====================================================

    public void normalize() {

        // =====================================================
        // FULL NAME
        // =====================================================

        if (

                this.fullName == null &&

                        this.name != null
        ) {

            this.fullName = this.name;
        }

        // =====================================================
        // APPLICANT NAME
        // =====================================================

        if (

                this.applicantName == null &&

                        this.name != null
        ) {

            this.applicantName = this.name;
        }

        // =====================================================
        // PHONE FALLBACKS
        // =====================================================

        if (

                this.phone == null &&

                        this.phoneNumber != null
        ) {

            this.phone = this.phoneNumber;
        }

        if (

                this.mobile == null &&

                        this.phoneNumber != null
        ) {

            this.mobile = this.phoneNumber;
        }

        // =====================================================
        // PHOTO FALLBACKS
        // =====================================================

        if (

                this.profilePhotoUrl == null &&

                        this.photoUrl != null
        ) {

            this.profilePhotoUrl = this.photoUrl;
        }

        if (

                this.avatarUrl == null &&

                        this.photoUrl != null
        ) {

            this.avatarUrl = this.photoUrl;
        }

        // =====================================================
        // KYC STATUS
        // =====================================================

        if (

                this.kycStatus == null ||

                        this.kycStatus.isBlank()
        ) {

            if (Boolean.TRUE.equals(this.kycVerified)) {

                this.kycStatus = "VERIFIED";

            } else {

                this.kycStatus = "PENDING";
            }
        }

        // =====================================================
        // KYC OBJECT
        // =====================================================

        if (this.kyc != null) {

            this.kyc.normalize();
        }
    }

    // =====================================================
    // KYC
    // =====================================================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Kyc {

        private String status;

        private String documentType;

        private String documentNumber;

        private String filePath;

        // =====================================================
        // FRONTEND FALLBACK
        // =====================================================

        private String documentUrl;

        // =====================================================
        // REJECTION
        // =====================================================

        private String rejectionReason;

        // =====================================================
        // NORMALIZATION
        // =====================================================

        public void normalize() {

            if (this.documentUrl == null && this.filePath != null) {
                this.documentUrl = this.filePath;
            }

            if (this.status == null || this.status.isBlank()) {
                this.status = "PENDING";
            }

            if (

                    this.documentUrl == null &&

                            this.filePath != null
            ) {

                this.documentUrl = this.filePath;
            }

            if (

                    this.status == null ||

                            this.status.isBlank()
            ) {

                this.status = "PENDING";
            }


        }
    }
}