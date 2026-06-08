package in.bawvpl.Authify.repository;

import in.bawvpl.Authify.entity.KycAuditEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KycAuditRepository
        extends JpaRepository<KycAuditEntity, Long> {

    // =====================================================
    // GET AUDIT HISTORY BY KYC
    // =====================================================

    List<KycAuditEntity>
    findByKyc_IdOrderByCreatedAtDesc(
            Long kycId
    );

    // =====================================================
    // PAGINATED AUDIT HISTORY
    // =====================================================

    Page<KycAuditEntity>
    findByKyc_Id(

            Long kycId,

            Pageable pageable
    );

    // =====================================================
    // FILTER BY ACTION
    // =====================================================

    List<KycAuditEntity>
    findByActionOrderByCreatedAtDesc(
            String action
    );

    // =====================================================
    // FILTER BY REVIEWER
    // =====================================================

    List<KycAuditEntity>
    findByReviewedByOrderByCreatedAtDesc(
            String reviewedBy
    );

    // =====================================================
    // COUNT AUDIT EVENTS
    // =====================================================

    long countByKyc_Id(
            Long kycId
    );
}