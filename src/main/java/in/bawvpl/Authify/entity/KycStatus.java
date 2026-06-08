package in.bawvpl.Authify.entity;

/**
 * =====================================================
 * KYC STATUS
 * =====================================================
 *
 * IMPORTANT:
 * frontend contracts expect:
 *
 * - VERIFIED
 * - APPROVED
 * - UNDER_REVIEW
 * - REUPLOAD_REQUIRED
 * - REJECTED
 * - PENDING
 *
 * =====================================================
 */
public enum KycStatus {

    // =====================================================
    // DEFAULT
    // =====================================================

    PENDING,

    // =====================================================
    // REVIEW
    // =====================================================

    UNDER_REVIEW,

    // =====================================================
    // SUCCESS
    // =====================================================

    VERIFIED,

    APPROVED,

    // =====================================================
    // FAILURE
    // =====================================================

    REJECTED,

    REUPLOAD_REQUIRED;

    // =====================================================
    // HELPERS
    // =====================================================

    public boolean isApproved() {

        return this == VERIFIED ||

                this == APPROVED;
    }

    public boolean isRejected() {

        return this == REJECTED;
    }

    public boolean isPending() {

        return this == PENDING ||

                this == UNDER_REVIEW;
    }

    public boolean requiresReupload() {

        return this == REUPLOAD_REQUIRED;
    }
}