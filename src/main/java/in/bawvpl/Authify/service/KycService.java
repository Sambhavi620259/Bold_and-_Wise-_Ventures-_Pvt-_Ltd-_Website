package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.KycEntity;
import in.bawvpl.Authify.entity.KycStatus;

import in.bawvpl.Authify.io.AdminKycResponse;
import in.bawvpl.Authify.io.KycDetailResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

/**
 * =====================================================
 * KYC SERVICE
 * =====================================================
 */
public interface KycService {

    // =====================================================
    // USER
    // =====================================================

    String uploadKyc(

            String email,

            String documentType,

            String documentNumber,

            MultipartFile file
    );

    String resubmitKyc(

            String email,

            String documentType,

            String documentNumber,

            MultipartFile file
    );

    KycEntity getMyKyc(
            String email
    );

    Optional<KycEntity> findByUserEmail(
            String email
    );

    // =====================================================
    // USER DTO
    // =====================================================

    KycDetailResponse getMyKycDetail(
            String email
    );

    // =====================================================
    // STATUS
    // =====================================================

    String getKycStatus(
            String email
    );

    boolean isKycVerified(
            String email
    );

    // =====================================================
    // ADMIN ACTIONS
    // =====================================================

    /**
     * IMPORTANT:
     * Frontend now sends KYC table id
     * NOT UserEntity id
     */
    String verifyKyc(
            Long kycId
    );

    /**
     * IMPORTANT:
     * Frontend now sends KYC table id
     * NOT UserEntity id
     */
    String rejectKyc(

            Long kycId,

            String reason
    );

    // =====================================================
    // ADMIN LISTS
    // =====================================================

    List<KycEntity> getAllKyc();

    List<KycEntity> getPendingKyc();

    List<KycEntity> getByStatus(
            KycStatus status
    );

    // =====================================================
    // ADMIN DTO RESPONSES
    // =====================================================

    List<AdminKycResponse> getAdminKycResponses();

    List<AdminKycResponse> getPendingAdminKycResponses();

    /**
     * IMPORTANT:
     * This method still uses USER ID intentionally
     * because it fetches KYC by user.
     */
    AdminKycResponse getAdminKycByUserId(
            Long userId
    );

    // =====================================================
    // PAGINATION
    // =====================================================

    Page<KycEntity> getAllKyc(
            Pageable pageable
    );

    Page<KycEntity> getByStatus(

            KycStatus status,

            Pageable pageable
    );

    // =====================================================
    // SEARCH
    // =====================================================

    List<KycEntity> search(
            String query
    );

    // =====================================================
    // ANALYTICS
    // =====================================================

    long totalKyc();

    long totalPending();

    long totalVerified();

    long totalRejected();

    long totalTodayUploads();

    // =====================================================
    // HELPERS
    // =====================================================

    default boolean isApprovedStatus(
            KycStatus status
    ) {

        return status == KycStatus.VERIFIED ||

                status == KycStatus.APPROVED;
    }

    default boolean isRejectedStatus(
            KycStatus status
    ) {

        return status == KycStatus.REJECTED;
    }

    default boolean isPendingStatus(
            KycStatus status
    ) {

        return status == KycStatus.PENDING ||

                status == KycStatus.UNDER_REVIEW;
    }

    // =====================================================
    // SAFE STATUS
    // =====================================================

    default String safeStatus(
            KycStatus status
    ) {

        return status == null

                ? "PENDING"

                : status.name();
    }

    // =====================================================
    // CAN VERIFY
    // =====================================================

    default boolean canVerify(
            KycStatus status
    ) {

        return status == KycStatus.PENDING ||

                status == KycStatus.UNDER_REVIEW ||

                status == KycStatus.REUPLOAD_REQUIRED;
    }

    // =====================================================
    // CAN REJECT
    // =====================================================

    default boolean canReject(
            KycStatus status
    ) {

        return status != KycStatus.VERIFIED &&

                status != KycStatus.APPROVED;
    }
}