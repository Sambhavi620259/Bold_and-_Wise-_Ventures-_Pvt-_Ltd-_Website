package in.bawvpl.Authify.controller;

import in.bawvpl.Authify.io.ApiResponse;
import in.bawvpl.Authify.io.ReplyTicketRequest;
import in.bawvpl.Authify.io.TicketConversationResponse;
import in.bawvpl.Authify.io.TicketResponse;

import in.bawvpl.Authify.service.TicketService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1.0/tickets")
@RequiredArgsConstructor
@Slf4j
public class TicketController {

    private final TicketService ticketService;

    // =====================================================
    // AUTH USER EMAIL
    // =====================================================

    private String getEmail(Authentication auth) {

        if (
                auth == null ||
                        auth.getName() == null ||
                        auth.getName().isBlank()
        ) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Unauthorized"
            );
        }

        return auth.getName()
                .trim()
                .toLowerCase();
    }

    // =====================================================
    // CREATE TICKET
    // =====================================================

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<TicketResponse>> create(
            Authentication auth,
            @Valid @RequestBody CreateReq req
    ) {

        try {

            TicketResponse response =
                    ticketService.create(
                            getEmail(auth),
                            req.getSubject(),
                            req.getMessage()
                    );

            response.normalize();

            return ResponseEntity.ok(

                    ApiResponse.<TicketResponse>builder()

                            .success(true)

                            .status(200)

                            .message(
                                    "Ticket created successfully"
                            )

                            .data(response)

                            .meta(null)

                            .build()
            );

        } catch (ResponseStatusException e) {

            throw e;

        } catch (Exception e) {

            log.error(
                    "Create ticket failed",
                    e
            );

            return ResponseEntity
                    .status(500)
                    .body(

                            ApiResponse.<TicketResponse>builder()

                                    .success(false)

                                    .status(500)

                                    .message(
                                            e.getMessage()
                                    )

                                    .build()
                    );
        }
    }

    // =====================================================
    // MY TICKETS
    // =====================================================

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<?>> myTickets(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        try {

            page = Math.max(page, 0);

            if (size <= 0) {
                size = 10;
            }

            if (size > 100) {
                size = 100;
            }

            Page<TicketResponse> tickets =
                    ticketService.getUserTickets(
                            getEmail(auth),
                            page,
                            size
                    );

            tickets.forEach(TicketResponse::normalize);

            Map<String, Object> meta =
                    buildPageMeta(tickets);

            return ResponseEntity.ok(

                    ApiResponse.builder()

                            .success(true)

                            .status(200)

                            .message(
                                    "Tickets fetched successfully"
                            )

                            .data(
                                    tickets.getContent()
                            )

                            .meta(meta)

                            .build()
            );

        } catch (ResponseStatusException e) {

            throw e;

        } catch (Exception e) {

            log.error(
                    "Fetch tickets failed",
                    e
            );

            return ResponseEntity
                    .status(500)
                    .body(

                            ApiResponse.builder()

                                    .success(false)

                                    .status(500)

                                    .message(
                                            e.getMessage()
                                    )

                                    .build()
                    );
        }
    }

    // =====================================================
    // ADMIN USER-SCOPED TICKETS
    // =====================================================

    @PreAuthorize("hasAnyRole('ADMIN','OWNER')")
    @GetMapping("/admin")
    public ResponseEntity<ApiResponse<?>> adminTickets(

            @RequestParam(required = false)
            Long userId,

            @RequestParam(required = false)
            String userEmail,

            @RequestParam(required = false)
            String status,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size
    ) {

        try {

            page = Math.max(page, 0);

            if (size <= 0) {
                size = 10;
            }

            if (size > 100) {
                size = 100;
            }

            Page<TicketResponse> tickets =
                    ticketService.getAdminTickets(
                            userId,
                            userEmail,
                            status,
                            page,
                            size
                    );

            tickets.forEach(TicketResponse::normalize);

            Map<String, Object> meta =
                    buildPageMeta(tickets);

            return ResponseEntity.ok(

                    ApiResponse.builder()

                            .success(true)

                            .status(200)

                            .message(
                                    "Admin tickets fetched"
                            )

                            .data(
                                    tickets.getContent()
                            )

                            .meta(meta)

                            .build()
            );

        } catch (ResponseStatusException e) {

            throw e;

        } catch (Exception e) {

            log.error(
                    "Admin tickets failed",
                    e
            );

            return ResponseEntity
                    .status(500)
                    .body(

                            ApiResponse.builder()

                                    .success(false)

                                    .status(500)

                                    .message(
                                            e.getMessage()
                                    )

                                    .build()
                    );
        }
    }

    // =====================================================
    // ADMIN TICKETS COMPATIBILITY ENDPOINT
    // =====================================================

    @PreAuthorize("hasAnyRole('ADMIN','OWNER')")
    @GetMapping("/admin/tickets")
    public ResponseEntity<ApiResponse<?>> adminTicketsCompat(

            @RequestParam(required = false)
            Long userId,

            @RequestParam(required = false)
            String userEmail,

            @RequestParam(required = false)
            String status,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size
    ) {

        return adminTickets(
                userId,
                userEmail,
                status,
                page,
                size
        );
    }

    // =====================================================
    // FULL CONVERSATION
    // =====================================================

    @GetMapping("/{ticketId}")
    public ResponseEntity<ApiResponse<TicketConversationResponse>> detail(

            Authentication auth,

            @PathVariable
            Long ticketId
    ) {

        try {

            if (ticketId == null) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Ticket ID required"
                );
            }

            TicketConversationResponse response =
                    ticketService.getConversation(
                            ticketId,
                            getEmail(auth)
                    );

            return ResponseEntity.ok(

                    ApiResponse
                            .<TicketConversationResponse>builder()

                            .success(true)

                            .status(200)

                            .message(
                                    "Ticket conversation fetched successfully"
                            )

                            .data(response)

                            .meta(null)

                            .build()
            );

        } catch (ResponseStatusException e) {

            throw e;

        } catch (Exception e) {

            log.error(
                    "Ticket detail failed",
                    e
            );

            return ResponseEntity
                    .status(500)
                    .body(

                            ApiResponse
                                    .<TicketConversationResponse>builder()

                                    .success(false)

                                    .status(500)

                                    .message(
                                            e.getMessage()
                                    )

                                    .build()
                    );
        }
    }

    // =====================================================
    // REPLY TO TICKET
    // =====================================================

    @PostMapping("/{ticketId}/reply")
    public ResponseEntity<ApiResponse<String>> reply(

            Authentication auth,

            @PathVariable
            Long ticketId,

            @Valid
            @RequestBody
            ReplyTicketRequest request
    ) {

        try {

            if (ticketId == null) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Ticket ID required"
                );
            }

            ticketService.reply(
                    ticketId,
                    request.getMessage(),
                    getEmail(auth)
            );

            return ResponseEntity.ok(

                    ApiResponse.<String>builder()

                            .success(true)

                            .status(200)

                            .message(
                                    "Reply sent successfully"
                            )

                            .data("SUCCESS")

                            .meta(null)

                            .build()
            );

        } catch (ResponseStatusException e) {

            throw e;

        } catch (Exception e) {

            log.error(
                    "Reply ticket failed",
                    e
            );

            return ResponseEntity
                    .status(500)
                    .body(

                            ApiResponse.<String>builder()

                                    .success(false)

                                    .status(500)

                                    .message(
                                            e.getMessage()
                                    )

                                    .build()
                    );
        }
    }

    // =====================================================
    // CLOSE TICKET
    // =====================================================

    @PostMapping("/close/{ticketId}")
    public ResponseEntity<ApiResponse<String>> close(

            Authentication auth,

            @PathVariable
            Long ticketId
    ) {

        try {

            if (ticketId == null) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Ticket ID required"
                );
            }

            ticketService.close(
                    ticketId,
                    getEmail(auth)
            );

            return ResponseEntity.ok(

                    ApiResponse.<String>builder()

                            .success(true)

                            .status(200)

                            .message(
                                    "Ticket closed successfully"
                            )

                            .data("SUCCESS")

                            .meta(null)

                            .build()
            );

        } catch (ResponseStatusException e) {

            throw e;

        } catch (Exception e) {

            log.error(
                    "Close ticket failed",
                    e
            );

            return ResponseEntity
                    .status(500)
                    .body(

                            ApiResponse.<String>builder()

                                    .success(false)

                                    .status(500)

                                    .message(
                                            e.getMessage()
                                    )

                                    .build()
                    );
        }
    }

    // =====================================================
    // UPDATE TICKET STATUS
    // =====================================================

    @PreAuthorize("hasAnyRole('ADMIN','OWNER')")
    @PatchMapping("/{ticketId}/status")
    public ResponseEntity<ApiResponse<String>> updateStatus(

            Authentication auth,

            @PathVariable
            Long ticketId,

            @RequestParam
            String status
    ) {

        try {

            if (ticketId == null) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Ticket ID required"
                );
            }

            if (
                    status == null ||
                            status.isBlank()
            ) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Status required"
                );
            }

            String normalized =
                    status
                            .trim()
                            .toUpperCase();

            switch (normalized) {

                case "RESOLVED":

                    ticketService.resolve(
                            ticketId,
                            getEmail(auth)
                    );

                    break;

                case "CLOSED":

                    ticketService.close(
                            ticketId,
                            getEmail(auth)
                    );

                    break;

                default:

                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Unsupported status"
                    );
            }

            return ResponseEntity.ok(

                    ApiResponse.<String>builder()

                            .success(true)

                            .status(200)

                            .message(
                                    "Ticket status updated successfully"
                            )

                            .data(normalized)

                            .meta(null)

                            .build()
            );

        } catch (ResponseStatusException e) {

            throw e;

        } catch (Exception e) {

            log.error(
                    "Update ticket status failed",
                    e
            );

            return ResponseEntity
                    .status(500)
                    .body(

                            ApiResponse.<String>builder()

                                    .success(false)

                                    .status(500)

                                    .message(
                                            e.getMessage()
                                    )

                                    .build()
                    );
        }
    }

    // =====================================================
    // PAGE META
    // =====================================================

    private Map<String, Object> buildPageMeta(Page<?> page) {

        Map<String, Object> meta =
                new HashMap<>();

        meta.put(
                "page",
                page.getNumber()
        );

        meta.put(
                "size",
                page.getSize()
        );

        meta.put(
                "totalPages",
                page.getTotalPages()
        );

        meta.put(
                "totalElements",
                page.getTotalElements()
        );

        meta.put(
                "hasNext",
                page.hasNext()
        );

        meta.put(
                "hasPrevious",
                page.hasPrevious()
        );

        meta.put(
                "content",
                page.getContent()
        );

        return meta;
    }

    // =====================================================
    // CREATE DTO
    // =====================================================

    @Data
    public static class CreateReq {

        @NotBlank(
                message = "Subject is required"
        )
        private String subject;

        @NotBlank(
                message = "Message is required"
        )
        private String message;
    }
}