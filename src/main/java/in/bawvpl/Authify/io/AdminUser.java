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
public class AdminUser {

    // =====================================================
    // IDS
    // =====================================================

    private Long id;

    private String userId;

    // =====================================================
// REFERRAL
// =====================================================

    private String referredByUserId;

    // =====================================================
    // USER
    // =====================================================

    private String name;

    private String fullName;

    private String email;

    private String phoneNumber;



    // =====================================================
    // ROLE
    // =====================================================

    private String role;

    // =====================================================
    // STATUS
    // =====================================================

    private String status;

    // =====================================================
    // KYC
    // =====================================================

    private String kycStatus;

    // =====================================================
    // CREATED
    // =====================================================

    private String createdAt;

    // =====================================================
    // FALLBACKS
    // =====================================================

    private String applicantName;

    private String mobile;

    private String phone;

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
        // ROLE
        // =====================================================

        if (

                this.role == null ||

                        this.role.isBlank()
        ) {

            this.role = "ROLE_USER";
        }

        // =====================================================
        // STATUS
        // =====================================================

        if (

                this.status == null ||

                        this.status.isBlank()
        ) {

            this.status = "ACTIVE";
        }

        // =====================================================
        // KYC
        // =====================================================

        if (

                this.kycStatus == null ||

                        this.kycStatus.isBlank()
        ) {

            this.kycStatus = "PENDING";
        }
    }
}