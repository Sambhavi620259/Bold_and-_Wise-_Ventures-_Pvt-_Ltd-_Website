package in.bawvpl.Authify.io;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TicketMessageResponse {

    // =====================================================
    // MESSAGE ID
    // =====================================================

    private Long id;

    // =====================================================
    // SENDER TYPE
    // USER / ADMIN
    // =====================================================

    private String senderType;

    // =====================================================
    // MESSAGE CONTENT
    // =====================================================

    private String message;

    // =====================================================
    // CREATED TIME
    // =====================================================

    private LocalDateTime createdAt;
}