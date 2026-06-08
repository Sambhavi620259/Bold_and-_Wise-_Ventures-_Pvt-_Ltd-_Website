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
public class KycStatusUpdateRequest {

    // =====================================================
    // STATUS
    // =====================================================

    private String status;

    // =====================================================
    // REJECTION REASON
    // =====================================================

    private String rejectionReason;

    // =====================================================
    // OPTIONAL MESSAGE
    // =====================================================

    private String message;

    // =====================================================
    // NORMALIZE
    // =====================================================

    public void normalize() {

        if (this.status != null) {

            this.status =
                    this.status
                            .trim()
                            .toUpperCase();
        }

        if (this.rejectionReason != null) {

            this.rejectionReason =
                    this.rejectionReason.trim();
        }

        if (this.message != null) {

            this.message =
                    this.message.trim();
        }
    }

    // =====================================================
    // CANONICAL STATUS
    // =====================================================

    public String getCanonicalStatus() {

        if (

                this.status == null ||

                        this.status.isBlank()
        ) {

            return "PENDING";
        }

        String value =
                this.status
                        .trim()
                        .toUpperCase();

        return switch (value) {

            case "APPROVED" -> "VERIFIED";

            case "VERIFIED" -> "VERIFIED";

            case "UNDER_REVIEW" -> "UNDER_REVIEW";

            case "REUPLOAD_REQUIRED" -> "REUPLOAD_REQUIRED";

            case "REJECTED" -> "REJECTED";

            case "PENDING" -> "PENDING";

            default -> "PENDING";
        };
    }

    // =====================================================
    // HELPERS
    // =====================================================

    public boolean isRejected() {

        return "REJECTED"
                .equalsIgnoreCase(
                        getCanonicalStatus()
                );
    }

    public boolean isVerified() {

        return "VERIFIED"
                .equalsIgnoreCase(
                        getCanonicalStatus()
                );
    }

    public boolean isPending() {

        return "PENDING"
                .equalsIgnoreCase(
                        getCanonicalStatus()
                );
    }

    public boolean isUnderReview() {

        return "UNDER_REVIEW"
                .equalsIgnoreCase(
                        getCanonicalStatus()
                );
    }

    public boolean isReuploadRequired() {

        return "REUPLOAD_REQUIRED"
                .equalsIgnoreCase(
                        getCanonicalStatus()
                );
    }

    // =====================================================
    // SAFE REJECTION REASON
    // =====================================================

    public String getSafeRejectionReason() {

        if (

                this.rejectionReason == null ||

                        this.rejectionReason.isBlank()
        ) {

            return "KYC rejected";
        }

        return this.rejectionReason.trim();
    }
}