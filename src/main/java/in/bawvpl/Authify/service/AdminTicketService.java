package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.TicketEntity;
import in.bawvpl.Authify.entity.TicketMessageEntity;
import in.bawvpl.Authify.entity.UserEntity;

import in.bawvpl.Authify.io.AdminTicket;

import in.bawvpl.Authify.repository.TicketMessageRepository;
import in.bawvpl.Authify.repository.TicketRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.http.HttpStatus;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

import java.util.List;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminTicketService {

    private final TicketRepository ticketRepository;

    private final TicketMessageRepository messageRepository;

    // =====================================================
    // OPEN TICKETS COUNT
    // =====================================================

    @Transactional(readOnly = true)
    public long openTicketsCount() {

        return ticketRepository.countByStatus(
                TicketEntity.Status.OPEN
        );
    }

    // =====================================================
    // GET TICKETS
    // =====================================================

    @Transactional(readOnly = true)
    public List<AdminTicket> getTickets(

            String status,

            Long userId,

            int page,

            int size
    ) {

        Pageable pageable =
                PageRequest.of(page, size);

        Page<TicketEntity> ticketPage;

        // =====================================================
        // STATUS PARSING
        // =====================================================

        TicketEntity.Status ticketStatus = null;

        if (
                status != null &&
                        !status.isBlank()
        ) {

            try {

                ticketStatus =
                        TicketEntity.Status.valueOf(

                                status
                                        .trim()
                                        .toUpperCase()
                        );

            } catch (Exception e) {

                throw new ResponseStatusException(

                        HttpStatus.BAD_REQUEST,
                        "Invalid ticket status"
                );
            }
        }

        // =====================================================
        // QUERY SELECTION
        // =====================================================

        if (
                userId != null &&
                        ticketStatus != null
        ) {

            // USER + STATUS

            ticketPage =
                    ticketRepository
                            .findByUser_IdAndStatus(

                                    userId,

                                    ticketStatus,

                                    pageable
                            );
        }

        else if (userId != null) {

            // USER ONLY

            ticketPage =
                    ticketRepository
                            .findByUser_Id(

                                    userId,

                                    pageable
                            );
        }

        else if (ticketStatus != null) {

            // STATUS ONLY

            ticketPage =
                    ticketRepository
                            .findByStatus(

                                    ticketStatus,

                                    pageable
                            );
        }

        else {

            // ALL

            ticketPage =
                    ticketRepository
                            .findAllByOrderByCreatedAtDesc(
                                    pageable
                            );
        }

        // =====================================================
        // RESPONSE
        // =====================================================

        return ticketPage
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // =====================================================
    // RESOLVE TICKET
    // =====================================================

    public void resolveTicket(
            String id
    ) {

        if (
                id == null ||
                        id.isBlank()
        ) {

            throw new ResponseStatusException(

                    HttpStatus.BAD_REQUEST,
                    "Ticket ID required"
            );
        }

        Long ticketId;

        try {

            ticketId =
                    Long.parseLong(id);

        } catch (Exception e) {

            throw new ResponseStatusException(

                    HttpStatus.BAD_REQUEST,
                    "Invalid ticket ID"
            );
        }

        TicketEntity ticket =
                ticketRepository
                        .findById(ticketId)
                        .orElseThrow(() ->

                                new ResponseStatusException(

                                        HttpStatus.NOT_FOUND,
                                        "Ticket not found"
                                )
                        );

        ticket.setStatus(
                TicketEntity.Status.RESOLVED
        );

        ticket.setUpdatedAt(
                LocalDateTime.now()
        );

        ticketRepository.save(ticket);
    }

    // =====================================================
// UPDATE TICKET STATUS
// =====================================================

    public void updateTicketStatus(

            String id,

            String status
    ) {

        if (
                status == null ||
                        status.isBlank()
        ) {

            status = "RESOLVED";
        }

        if (
                status.equalsIgnoreCase("RESOLVED")
        ) {

            resolveTicket(id);

            return;
        }

        if (
                id == null ||
                        id.isBlank()
        ) {

            throw new ResponseStatusException(

                    HttpStatus.BAD_REQUEST,
                    "Ticket ID required"
            );
        }

        Long ticketId;

        try {

            ticketId =
                    Long.parseLong(id);

        } catch (Exception e) {

            throw new ResponseStatusException(

                    HttpStatus.BAD_REQUEST,
                    "Invalid ticket ID"
            );
        }

        TicketEntity ticket =
                ticketRepository
                        .findById(ticketId)
                        .orElseThrow(() ->

                                new ResponseStatusException(

                                        HttpStatus.NOT_FOUND,
                                        "Ticket not found"
                                )
                        );

        try {

            ticket.setStatus(

                    TicketEntity.Status.valueOf(

                            status
                                    .trim()
                                    .toUpperCase()
                    )
            );

        } catch (Exception e) {

            throw new ResponseStatusException(

                    HttpStatus.BAD_REQUEST,
                    "Invalid ticket status"
            );
        }

        ticket.setUpdatedAt(
                LocalDateTime.now()
        );

        ticketRepository.save(ticket);
    }

    // =====================================================
    // DTO MAPPER
    // =====================================================

    private AdminTicket mapToResponse(
            TicketEntity ticket
    ) {

        if (ticket == null) {

            return AdminTicket.builder().build();
        }

        UserEntity user =
                ticket.getUser();

        // =====================================================
        // FIRST MESSAGE
        // =====================================================

        String description = "";

        List<TicketMessageEntity> messages =
                messageRepository
                        .findByTicket_IdOrderByCreatedAtAsc(
                                ticket.getId()
                        );

        if (
                messages != null &&
                        !messages.isEmpty()
        ) {

            TicketMessageEntity firstMessage =
                    messages.get(0);

            if (
                    firstMessage != null &&
                            firstMessage.getMessage() != null
            ) {

                description =
                        firstMessage.getMessage();
            }
        }

        return AdminTicket.builder()

                // =====================================================
                // TICKET
                // =====================================================

                .id(
                        ticket.getId() != null

                                ? String.valueOf(
                                ticket.getId()
                        )

                                : ""
                )

                .issue(
                        ticket.getSubject() != null

                                ? ticket.getSubject()

                                : ""
                )

                .description(
                        description
                )

                .status(
                        ticket.getStatus() != null

                                ? ticket.getStatus().name()

                                : "OPEN"
                )

                .createdAt(
                        ticket.getCreatedAt() != null

                                ? ticket.getCreatedAt().toString()

                                : null
                )

                // =====================================================
                // USER
                // =====================================================

                .userId(
                        user != null &&
                                user.getUserId() != null

                                ? user.getUserId()

                                : ""
                )

                .name(
                        user != null &&
                                user.getEntityName() != null

                                ? user.getEntityName()

                                : ""
                )

                .email(
                        user != null &&
                                user.getEmail() != null

                                ? user.getEmail()

                                : ""
                )

                .phone(
                        user != null &&
                                user.getPhoneNumber() != null

                                ? user.getPhoneNumber()

                                : ""
                )

                .build();
    }
}