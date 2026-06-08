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
public class ProfileRequest {

    // =====================================================
    // BASIC DETAILS
    // =====================================================

    private String name;

    private String fullName;

    private String email;

    private String phoneNumber;

    private String password;

    // =====================================================
    // FRONTEND FALLBACKS
    // =====================================================

    private String phone;

    private String mobile;

    private String applicantName;

    // =====================================================
    // ADDRESS
    // =====================================================

    private String address;

    // =====================================================
    // REFERRAL
    // =====================================================

    private String referralCode;

    // =====================================================
    // PROFILE PHOTO
    // =====================================================

    private String photoUrl;

    private String profilePhotoUrl;

    private String avatarUrl;

    // =====================================================
    // KYC
    // =====================================================

    private String kycStatus;

    // =====================================================
    // KYC DOCUMENT
    // =====================================================

    // Example:
    // AADHAAR
    // PAN
    // PASSPORT
    // DRIVING_LICENSE

    private String documentType;

    // Example:
    // Aadhaar number
    // PAN number

    private String documentNumber;

    // =====================================================
    // FILE URL / STORAGE PATH
    // =====================================================

    // IMPORTANT:
    // Save uploaded file URL/path here
    // Example:
    // https://server.com/uploads/aadhaar.pdf

    private String filePath;

    // =====================================================
    // FRONTEND FALLBACK
    // =====================================================

    private String documentUrl;

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

        if (

                this.name == null &&

                        this.fullName != null
        ) {

            this.name = this.fullName;
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

                this.phoneNumber == null &&

                        this.phone != null
        ) {

            this.phoneNumber = this.phone;
        }

        if (

                this.phoneNumber == null &&

                        this.mobile != null
        ) {

            this.phoneNumber = this.mobile;
        }

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

                this.photoUrl == null &&

                        this.profilePhotoUrl != null
        ) {

            this.photoUrl = this.profilePhotoUrl;
        }

        if (

                this.photoUrl == null &&

                        this.avatarUrl != null
        ) {

            this.photoUrl = this.avatarUrl;
        }

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
        // DOCUMENT URL
        // =====================================================

        if (

                this.documentUrl == null &&

                        this.filePath != null
        ) {

            this.documentUrl = this.filePath;
        }

        if (

                this.filePath == null &&

                        this.documentUrl != null
        ) {

            this.filePath = this.documentUrl;
        }

        // =====================================================
        // KYC STATUS
        // =====================================================

        if (

                this.kycStatus == null ||

                        this.kycStatus.isBlank()
        ) {

            this.kycStatus = "PENDING";
        }
    }
}