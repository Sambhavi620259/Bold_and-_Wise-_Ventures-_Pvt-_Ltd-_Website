package in.bawvpl.Authify.repository;

import in.bawvpl.Authify.entity.TicketMessageEntity;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketMessageRepository
        extends JpaRepository<TicketMessageEntity, Long> {

    // =====================================================
    // GET FULL CONVERSATION
    // IMPORTANT:
    // MUST ALWAYS RETURN COMPLETE HISTORY
    // =====================================================

    List<TicketMessageEntity>
    findByTicket_IdOrderByCreatedAtAsc(
            Long ticketId
    );

    // =====================================================
    // LATEST MESSAGE
    // =====================================================

    Optional<TicketMessageEntity>
    findTopByTicket_IdOrderByCreatedAtDesc(
            Long ticketId
    );

    // =====================================================
    // COUNT MESSAGES
    // =====================================================

    long countByTicket_Id(
            Long ticketId
    );

    // =====================================================
    // DELETE TICKET MESSAGES
    // =====================================================

    void deleteByTicket_Id(
            Long ticketId
    );
}