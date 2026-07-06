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
public class AdminUserResponse {

    // =====================================================
    // IDS
    // =====================================================

    private Long id;

    private String userId;

    // ===================================================== // REFERRAL
    // =====================================================
    private String referredByUserId;
    private String referredBy;

    // =====================================================
    // USER
    // =====================================================

    private String name;

    private String fullName;

    private String email;

    private String phoneNumber;

    // =====================================================
    // ENTITY TYPE
    // =====================================================

    private String entityType;

    // =====================================================
    // DOCUMENT URLS
    // =====================================================

    private String documentUrl;

    private String documentFile;

    private String frontDocumentUrl;

    private String backDocumentUrl;

    private String aadhaarFrontUrl;

    private String aadhaarBackUrl;

    private String panCardUrl;

    private String passportUrl;

    private String drivingLicenseUrl;

    private String selfieUrl;

    private String livePhotoUrl;

    // =====================================================
    // ROLE
    // =====================================================

    private String role;

    // =====================================================
    // KYC
    // VERIFIED
    // PENDING
    // REJECTED
    // NOT_SUBMITTED
    // =====================================================

    @Builder.Default
    private String kycStatus = "PENDING";

    // =====================================================
    // STATUS
    // ACTIVE
    // INACTIVE
    // BLOCKED
    // SUSPENDED
    // =====================================================

    @Builder.Default
    private String status = "ACTIVE";
    private String userStatus;

    // =====================================================
    // ACTIVE
    // =====================================================

    @Builder.Default
    private Boolean isActive = true;

    // =====================================================
    // TIMESTAMPS
    // =====================================================

    private LocalDateTime createdAt;

    // =====================================================
    // TICKETS
    // =====================================================

    @Builder.Default
    private Long openTicketsCount = 0L;

    // =====================================================
    // FRONTEND FALLBACKS
    // =====================================================

    private String applicantName;

    private String mobile;

    private String phone;

    // =====================================================
    // NORMALIZATION
    // =====================================================

    public void normalize() {

        // =====================================================
        // USER ID FALLBACK
        // =====================================================

        if (

                this.userId == null &&

                        this.id != null
        ) {

            this.userId = String.valueOf(this.id);
        }

        // =====================================================
        // FULL NAME FALLBACK
        // =====================================================

        if (

                this.fullName == null &&

                        this.name != null
        ) {

            this.fullName = this.name;
        }

        // =====================================================
        // APPLICANT NAME FALLBACK
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
        // STATUS DEFAULT
        // =====================================================

        if (

                this.status == null ||

                        this.status.isBlank()
        ) {

            this.status = "ACTIVE";
        }

        // =====================================================
        // KYC DEFAULT
        // =====================================================

        if (

                this.kycStatus == null ||

                        this.kycStatus.isBlank()
        ) {

            this.kycStatus = "PENDING";
        }

        // =====================================================
        // ACTIVE FLAG
        // =====================================================

        if (this.isActive == null) {

            this.isActive =
                    "ACTIVE".equalsIgnoreCase(
                            this.status
                    );
        }

        // =====================================================
        // ROLE DEFAULT
        // =====================================================

        if (

                this.role == null ||

                        this.role.isBlank()
        ) {

            this.role = "USER";
        }

        // =====================================================
        // OPEN TICKETS DEFAULT
        // =====================================================

        if (this.openTicketsCount == null) {

            this.openTicketsCount = 0L;
        }

        // =====================================================
        // ENTITY TYPE DEFAULT
        // =====================================================

        if (this.entityType == null || this.entityType.isBlank()) {
            this.entityType = "INDIVIDUAL";
        }
        // =====================================================
        // USER STATUS FALLBACK
        // =====================================================

        if (this.userStatus == null || this.userStatus.isBlank()) {
            this.userStatus = this.status;
        }

        // =====================================================
        // REFERRED BY FALLBACK
        // =====================================================

        if (this.referredBy == null) {
            this.referredBy = this.referredByUserId;
        }
    }


}