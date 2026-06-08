package in.bawvpl.Authify.io;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminTicket {

    // =====================================================
    // TICKET
    // =====================================================

    private String id;

    // =====================================================
    // SUBJECT / ISSUE
    // =====================================================

    private String issue;

    // =====================================================
    // INITIAL USER MESSAGE
    // =====================================================

    private String description;

    // =====================================================
    // STATUS
    // OPEN / IN_PROGRESS / RESOLVED / CLOSED
    // =====================================================

    private String status;

    // =====================================================
    // CREATED TIME
    // =====================================================

    private String createdAt;

    // =====================================================
    // USER
    // =====================================================

    private String userId;

    private String name;

    private String email;

    private String phone;
}