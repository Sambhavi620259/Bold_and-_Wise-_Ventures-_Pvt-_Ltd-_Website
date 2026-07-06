package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.NotificationEntity;
import in.bawvpl.Authify.entity.TicketEntity;
import in.bawvpl.Authify.entity.TicketMessageEntity;
import in.bawvpl.Authify.entity.UserEntity;

import in.bawvpl.Authify.io.TicketConversationResponse;
import in.bawvpl.Authify.io.TicketMessageResponse;
import in.bawvpl.Authify.io.TicketResponse;

import in.bawvpl.Authify.repository.NotificationRepository;
import in.bawvpl.Authify.repository.TicketMessageRepository;
import in.bawvpl.Authify.repository.TicketRepository;
import in.bawvpl.Authify.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.http.HttpStatus;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TicketService {

    private final TicketRepository ticketRepository;

    private final TicketMessageRepository messageRepository;

    private final NotificationRepository notificationRepository;

    private final UserRepository userRepository;

    // =====================================================
    // CREATE TICKET
    // =====================================================

    public TicketResponse create(

            String email,

            String subject,

            String message
    ) {

        validateText(subject, "Subject");

        validateText(message, "Message");

        UserEntity user =
                findUser(email);

        TicketEntity ticket =
                ticketRepository.save(

                        TicketEntity.builder()

                                .user(user)

                                .subject(subject.trim())

                                .status(TicketEntity.Status.OPEN)

                                .createdAt(LocalDateTime.now())

                                .updatedAt(LocalDateTime.now())

                                .build()
                );

        TicketMessageEntity firstMessage =
                TicketMessageEntity.builder()

                        .ticket(ticket)

                        .senderType("USER")

                        .message(message.trim())

                        .createdAt(LocalDateTime.now())

                        .build();

        messageRepository.save(firstMessage);

        // =====================================================
// ADMIN NOTIFICATION
// =====================================================

        userRepository.findAll()

                .stream()

                .filter(u ->

                        u.getRole() != null &&

                                (
                                        u.getRole().equalsIgnoreCase("ROLE_ADMIN") ||

                                                u.getRole().equalsIgnoreCase("ROLE_OWNER") ||

                                                u.getRole().equalsIgnoreCase("ADMIN") ||

                                                u.getRole().equalsIgnoreCase("OWNER")
                                )
                )

                .forEach(admin ->

                        notificationRepository.save(

                                NotificationEntity.builder()

                                        .user(admin)

                                        .title("New Ticket Created")

                                        .message(

                                                "New support ticket created by " +

                                                        user.getEntityName()
                                        )

                                        .type("ADMIN")

                                        .read(false)

                                        .createdAt(LocalDateTime.now())

                                        .build()
                        )
                );

        log.info(
                "Ticket created: {} by {}",
                ticket.getId(),
                email
        );

        return toResponse(ticket);
    }

    // =====================================================
    // USER TICKETS
    // =====================================================

    @Transactional(readOnly = true)
    public Page<TicketResponse> getUserTickets(

            String email,

            int page,

            int size
    ) {

        UserEntity user =
                findUser(email);

        Pageable pageable =
                createPageable(page, size);

        List<TicketEntity> allTickets =
                ticketRepository
                        .findByUser_IdOrderByCreatedAtDesc(
                                user.getId()
                        );

        List<TicketResponse> content =

                allTickets.stream()

                        .skip((long) page * size)

                        .limit(size)

                        .map(this::toResponse)

                        .filter(Objects::nonNull)

                        .collect(Collectors.toList());

        return new PageImpl<>(

                content,

                pageable,

                allTickets.size()
        );
    }

    // =====================================================
    // ADMIN TICKETS
    // IMPORTANT:
    // supports:
    // userId
    // userEmail
    // =====================================================

    @Transactional(readOnly = true)
    public Page<TicketResponse> getAdminTickets(

            Long userId,

            String userEmail,

            String status,

            int page,

            int size
    ) {

        Pageable pageable =
                createPageable(page, size);

        List<TicketEntity> allTickets =
                ticketRepository.findAll();

        // =====================================================
        // USER ID FILTER
        // =====================================================

        if (userId != null) {

            allTickets =

                    allTickets.stream()

                            .filter(t ->

                                    t.getUser() != null &&

                                            t.getUser().getId() != null &&

                                            t.getUser()
                                                    .getId()
                                                    .equals(userId)
                            )

                            .collect(Collectors.toList());
        }

        // =====================================================
        // USER EMAIL FILTER
        // =====================================================

        if (

                userEmail != null &&

                        !userEmail.isBlank()
        ) {

            String normalized =
                    userEmail
                            .trim()
                            .toLowerCase();

            allTickets =

                    allTickets.stream()

                            .filter(t ->

                                    t.getUser() != null &&

                                            t.getUser().getEmail() != null &&

                                            t.getUser()
                                                    .getEmail()
                                                    .trim()
                                                    .toLowerCase()
                                                    .equals(normalized)
                            )

                            .collect(Collectors.toList());
        }

        // =====================================================
        // STATUS FILTER
        // =====================================================

        if (

                status != null &&

                        !status.isBlank()
        ) {

            String normalized =
                    status
                            .trim()
                            .toUpperCase();

            allTickets =

                    allTickets.stream()

                            .filter(t ->

                                    t.getStatus() != null &&

                                            t.getStatus()
                                                    .name()
                                                    .equalsIgnoreCase(normalized)
                            )

                            .collect(Collectors.toList());
        }

        List<TicketResponse> content =

                allTickets.stream()

                        .sorted(

                                Comparator.comparing(
                                        TicketEntity::getCreatedAt
                                ).reversed()
                        )

                        .skip((long) page * size)

                        .limit(size)

                        .map(this::toResponse)

                        .filter(Objects::nonNull)

                        .collect(Collectors.toList());

        return new PageImpl<>(

                content,

                pageable,

                allTickets.size()
        );
    }

    // =====================================================
    // ALL TICKETS
    // =====================================================

    @Transactional(readOnly = true)
    public List<TicketResponse> getAllTickets() {

        return ticketRepository
                .findAll()

                .stream()

                .sorted(

                        Comparator.comparing(
                                TicketEntity::getCreatedAt
                        ).reversed()
                )

                .map(this::toResponse)

                .filter(Objects::nonNull)

                .collect(Collectors.toList());
    }

    // =====================================================
    // CONVERSATION
    // =====================================================

    @Transactional(readOnly = true)
    public TicketConversationResponse getConversation(

            Long ticketId,

            String email
    ) {

        UserEntity user =
                findUser(email);

        TicketEntity ticket =
                findTicket(ticketId);

        boolean isAdmin =
                isAdmin();

        if (!isAdmin) {

            validateTicketOwnership(
                    ticket,
                    user
            );
        }

        List<TicketMessageResponse> messages =

                messageRepository.findAll()

                        .stream()

                        .filter(m ->

                                m.getTicket() != null &&

                                        m.getTicket().getId() != null &&

                                        m.getTicket()
                                                .getId()
                                                .equals(ticketId)
                        )

                        .sorted(

                                Comparator.comparing(
                                        TicketMessageEntity::getCreatedAt
                                )
                        )

                        .map(message ->

                                TicketMessageResponse.builder()

                                        .id(message.getId())

                                        .senderType(
                                                message.getSenderType()
                                        )

                                        .message(
                                                message.getMessage()
                                        )

                                        .createdAt(
                                                message.getCreatedAt()
                                        )

                                        .build()
                        )

                        .collect(Collectors.toList());

        return TicketConversationResponse.builder()

                .id(ticket.getId())

                .title(ticket.getSubject())

                .status(

                        ticket.getStatus() != null

                                ? ticket.getStatus().name()

                                : "OPEN"
                )

                .createdAt(ticket.getCreatedAt())

                .messages(messages)

                .build();
    }

    // =====================================================
    // REPLY
    // =====================================================

    public void reply(

            Long ticketId,

            String message,

            String email
    ) {

        validateText(message, "Message");

        UserEntity user =
                findUser(email);

        TicketEntity ticket =
                findTicket(ticketId);

        if (

                ticket.getStatus() == TicketEntity.Status.CLOSED ||

                        ticket.getStatus() == TicketEntity.Status.RESOLVED
        ) {

            throw new ResponseStatusException(

                    HttpStatus.BAD_REQUEST,

                    "Ticket already closed"
            );
        }

        boolean isAdmin =
                isAdmin();

        if (!isAdmin) {

            validateTicketOwnership(
                    ticket,
                    user
            );
        }

        String senderType =
                isAdmin
                        ? "ADMIN"
                        : "USER";

        ticket.setUpdatedAt(
                LocalDateTime.now()
        );

        ticketRepository.save(ticket);

        TicketMessageEntity reply =
                TicketMessageEntity.builder()

                        .ticket(ticket)

                        .senderType(senderType)

                        .message(message.trim())

                        .createdAt(LocalDateTime.now())

                        .build();

        messageRepository.save(reply);

        // =====================================================
        // USER NOTIFICATION
        // =====================================================

        if (

                isAdmin &&

                        ticket.getUser() != null
        ) {

            notificationRepository.save(

                    NotificationEntity.builder()

                            .user(ticket.getUser())

                            .title("Ticket Reply")

                            .message(
                                    "Admin replied to your ticket"
                            )

                            .read(false)

                            .createdAt(LocalDateTime.now())

                            .build()
            );
        }

        log.info(
                "Reply added to ticket {}",
                ticketId
        );
    }

    // =====================================================
    // CLOSE
    // =====================================================

    public void close(

            Long ticketId,

            String email
    ) {

        UserEntity user =
                findUser(email);

        TicketEntity ticket =
                findTicket(ticketId);

        boolean isAdmin =
                isAdmin();

        if (!isAdmin) {

            validateTicketOwnership(
                    ticket,
                    user
            );
        }

        ticket.setStatus(
                TicketEntity.Status.CLOSED
        );

        ticket.setUpdatedAt(
                LocalDateTime.now()
        );

        ticketRepository.save(ticket);

        log.info(
                "Ticket closed: {}",
                ticketId
        );
    }

    // =====================================================
    // RESOLVE
    // =====================================================

    public void resolve(

            Long ticketId,

            String email
    ) {

        UserEntity user =
                findUser(email);

        TicketEntity ticket =
                findTicket(ticketId);

        boolean isAdmin =
                isAdmin();

        if (!isAdmin) {

            throw new ResponseStatusException(

                    HttpStatus.FORBIDDEN,

                    "Only admin can resolve tickets"
            );
        }

        ticket.setStatus(
                TicketEntity.Status.RESOLVED
        );

        ticket.setUpdatedAt(
                LocalDateTime.now()
        );

        ticketRepository.save(ticket);

        // =====================================================
        // USER NOTIFICATION
        // =====================================================

        if (ticket.getUser() != null) {

            notificationRepository.save(

                    NotificationEntity.builder()

                            .user(ticket.getUser())

                            .title("Ticket Resolved")

                            .message(
                                    "Your support ticket has been resolved"
                            )

                            .read(false)

                            .createdAt(LocalDateTime.now())

                            .build()
            );
        }

        log.info(
                "Ticket resolved: {}",
                ticketId
        );
    }

    // =====================================================
    // USER
    // =====================================================

    private UserEntity findUser(
            String email
    ) {

        return userRepository
                .findByEmailIgnoreCase(

                        email.trim()
                                .toLowerCase()
                )
                .orElseThrow(() ->

                        new ResponseStatusException(

                                HttpStatus.NOT_FOUND,

                                "User not found"
                        )
                );
    }

    // =====================================================
    // TICKET
    // =====================================================

    private TicketEntity findTicket(
            Long ticketId
    ) {

        return ticketRepository
                .findById(ticketId)

                .orElseThrow(() ->

                        new ResponseStatusException(

                                HttpStatus.NOT_FOUND,

                                "Ticket not found"
                        )
                );
    }

    // =====================================================
    // ADMIN
    // =====================================================

    private boolean isAdmin() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        return authentication != null &&

                authentication
                        .getAuthorities()
                        .stream()

                        .anyMatch(auth ->

                                auth.getAuthority().equals("ROLE_ADMIN")

                                        ||

                                        auth.getAuthority().equals("ROLE_OWNER")
                        );
    }

    // =====================================================
    // OWNERSHIP
    // =====================================================

    private void validateTicketOwnership(

            TicketEntity ticket,

            UserEntity user
    ) {

        if (

                ticket == null ||

                        ticket.getUser() == null ||

                        ticket.getUser().getId() == null ||

                        user == null ||

                        user.getId() == null ||

                        !ticket.getUser()
                                .getId()
                                .equals(user.getId())
        ) {

            throw new ResponseStatusException(

                    HttpStatus.FORBIDDEN,

                    "Unauthorized"
            );
        }
    }

    // =====================================================
    // VALIDATION
    // =====================================================

    private void validateText(

            String value,

            String field
    ) {

        if (

                value == null ||

                        value.trim().isBlank()
        ) {

            throw new ResponseStatusException(

                    HttpStatus.BAD_REQUEST,

                    field + " required"
            );
        }
    }

    // =====================================================
    // PAGINATION
    // =====================================================

    private Pageable createPageable(

            int page,

            int size
    ) {

        if (page < 0) {
            page = 0;
        }

        if (size <= 0) {
            size = 10;
        }

        if (size > 100) {
            size = 100;
        }

        return PageRequest.of(

                page,

                size,

                Sort.by("createdAt")
                        .descending()
        );
    }

    // =====================================================
    // DTO
    // =====================================================

    private TicketResponse toResponse(
            TicketEntity ticket
    ) {

        String status =

                ticket.getStatus() != null

                        ? ticket.getStatus().name()

                        : "OPEN";

        String firstMessage = null;

        List<TicketMessageEntity> messages =

                messageRepository.findAll()

                        .stream()

                        .filter(m ->

                                m.getTicket() != null &&

                                        m.getTicket().getId() != null &&

                                        m.getTicket()
                                                .getId()
                                                .equals(ticket.getId())
                        )

                        .sorted(

                                Comparator.comparing(
                                        TicketMessageEntity::getCreatedAt
                                )
                        )

                        .collect(Collectors.toList());

        if (!messages.isEmpty()) {

            firstMessage =
                    messages.get(0)
                            .getMessage();
        }

        TicketResponse response =

                TicketResponse.builder()

                        .id(ticket.getId())

                        .subject(ticket.getSubject())

                        .message(firstMessage)

                        .status(status)

                        .userId(

                                ticket.getUser() != null

                                        ? ticket.getUser().getId()

                                        : null
                        )

                        .userEmail(

                                ticket.getUser() != null

                                        ? ticket.getUser().getEmail()

                                        : null
                        )

                        .userName(

                                ticket.getUser() != null

                                        ? ticket.getUser().getEntityName()

                                        : null
                        )

                        .createdAt(
                                ticket.getCreatedAt()
                        )

                        .updatedAt(
                                ticket.getUpdatedAt()
                        )

                        .build();

        response.normalize();

        return response;
    }
}