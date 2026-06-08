package in.bawvpl.Authify.repository;

import in.bawvpl.Authify.entity.TransactionEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository
        extends JpaRepository<TransactionEntity, Long> {

    // =====================================================
    // USER TRANSACTIONS
    //
    // IMPORTANT:
    //
    // Used by:
    // - dashboard transactions
    // - payment history
    // - /transactions
    // - /transactions/my
    //
    // Frontend expects:
    // - paginated response
    // - content
    // - totalElements
    // =====================================================

    Page<TransactionEntity>
    findByUser_Id(

            Long userId,

            Pageable pageable
    );

    // =====================================================
    // USER + STATUS
    //
    // IMPORTANT:
    //
    // Supports:
    // - SUCCESS
    // - FAILED
    // - PENDING
    // - COMPLETED
    // =====================================================

    Page<TransactionEntity>
    findByUser_IdAndStatus(

            Long userId,

            String status,

            Pageable pageable
    );

    // =====================================================
    // SORTED PAYMENT HISTORY
    //
    // IMPORTANT:
    //
    // Newest first.
    // =====================================================

    Page<TransactionEntity>
    findByUser_IdOrderByPaymentDateDesc(

            Long userId,

            Pageable pageable
    );

    // =====================================================
    // TOTAL TRANSACTION COUNT
    //
    // Used by:
    // - dashboard summary
    // - KPI cards
    // =====================================================

    long countByUser_Id(
            Long userId
    );

    // =====================================================
    // SUCCESSFUL TRANSACTIONS COUNT
    // =====================================================

    long countByUser_IdAndStatus(

            Long userId,

            String status
    );
}