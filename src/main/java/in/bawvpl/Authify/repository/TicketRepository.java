package in.bawvpl.Authify.repository;

import in.bawvpl.Authify.entity.TicketEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository
        extends JpaRepository<TicketEntity, Long> {

    // =====================================================
    // USER TICKETS
    // =====================================================

    List<TicketEntity>
    findByUser_IdOrderByCreatedAtDesc(
            Long userId
    );

    // =====================================================
    // USER + STATUS
    // =====================================================

    List<TicketEntity>
    findByUser_IdAndStatusOrderByCreatedAtDesc(

            Long userId,

            TicketEntity.Status status
    );

    // =====================================================
    // ADMIN STATUS FILTER
    // =====================================================

    List<TicketEntity>
    findByStatusOrderByCreatedAtDesc(
            TicketEntity.Status status
    );

    // =====================================================
    // ALL TICKETS
    // =====================================================

    List<TicketEntity>
    findAllByOrderByCreatedAtDesc();

    // =====================================================
    // FIX:
    // LOAD ALL TICKETS WITH USER + MESSAGES
    // PREVENT LAZY LOAD ISSUES
    // =====================================================

    @EntityGraph(attributePaths = {
            "messages",
            "user"
    })
    List<TicketEntity>
    findAllWithMessagesByOrderByCreatedAtDesc();

    // =====================================================
    // ADMIN PAGINATION
    // =====================================================

    Page<TicketEntity>
    findAllByOrderByCreatedAtDesc(
            Pageable pageable
    );

    // =====================================================
    // FILTER BY USER
    // =====================================================

    Page<TicketEntity>
    findByUser_Id(

            Long userId,

            Pageable pageable
    );

    // =====================================================
    // FILTER BY STATUS
    // =====================================================

    Page<TicketEntity>
    findByStatus(

            TicketEntity.Status status,

            Pageable pageable
    );

    // =====================================================
    // FILTER BY USER + STATUS
    // =====================================================

    Page<TicketEntity>
    findByUser_IdAndStatus(

            Long userId,

            TicketEntity.Status status,

            Pageable pageable
    );

    // =====================================================
    // FIX:
    // LOAD FULL CONVERSATION AFTER RELOGIN
    // =====================================================

    @EntityGraph(attributePaths = {
            "messages",
            "user"
    })
    Optional<TicketEntity>
    findWithMessagesById(
            Long id
    );

    // =====================================================
    // COUNT BY STATUS
    // =====================================================

    long countByStatus(
            TicketEntity.Status status
    );

    // =====================================================
    // COUNT USER TICKETS
    // =====================================================

    long countByUser_Id(
            Long userId
    );
}