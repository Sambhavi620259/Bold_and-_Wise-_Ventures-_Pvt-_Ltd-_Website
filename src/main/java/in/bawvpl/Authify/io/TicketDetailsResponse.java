package in.bawvpl.Authify.io;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TicketDetailsResponse {

    // =====================================================
    // TICKET ID
    // =====================================================

    private Long id;

    // =====================================================
    // SUBJECT
    // =====================================================

    private String subject;

    // =====================================================
    // STATUS
    // =====================================================

    private String status;

    // =====================================================
    // USER INFO
    // =====================================================

    private Long userId;

    private String userName;

    private String userEmail;

    // =====================================================
    // CREATED / UPDATED
    // =====================================================

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // =====================================================
    // FULL CONVERSATION
    // =====================================================

    private List<TicketMessageResponse> messages;

    // =====================================================
    // HELPERS
    // =====================================================

    private Integer totalMessages;

    private Boolean closed;
}