package in.bawvpl.Authify.io;

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
public class TicketConversationResponse {

    // =====================================================
    // TICKET
    // =====================================================

    private Long id;

    // =====================================================
    // SUBJECT / TITLE
    // =====================================================

    private String title;

    // =====================================================
    // STATUS
    // OPEN / IN_PROGRESS / RESOLVED / CLOSED
    // =====================================================

    private String status;

    // =====================================================
    // CREATED TIME
    // =====================================================

    private LocalDateTime createdAt;

    // =====================================================
    // CHAT MESSAGES
    // =====================================================

    private List<TicketMessageResponse> messages;
}