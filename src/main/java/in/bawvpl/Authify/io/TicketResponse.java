package in.bawvpl.Authify.io;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketResponse {

    // =====================================================
    // TICKET ID
    // =====================================================

    private Long id;

    // =====================================================
    // SUBJECT / TITLE
    // =====================================================

    private String subject;

    // =====================================================
    // INITIAL USER MESSAGE
    // =====================================================

    private String message;

    // =====================================================
    // STATUS
    // OPEN / IN_PROGRESS / RESOLVED / CLOSED
    // =====================================================

    @Builder.Default
    private String status = "OPEN";

    // =====================================================
    // USER INFO
    // =====================================================

    private Long userId;

    private String userEmail;

    private String userName;

    // =====================================================
    // FRONTEND FALLBACKS
    // =====================================================

    private String applicantName;

    private String name;

    private String email;

    // =====================================================
    // CREATED TIME
    // =====================================================

    private LocalDateTime createdAt;

    // =====================================================
    // UPDATED TIME
    // =====================================================

    private LocalDateTime updatedAt;

    // =====================================================
    // NORMALIZATION
    // =====================================================

    public void normalize() {

        // =====================================================
        // STATUS DEFAULT
        // =====================================================

        if (

                this.status == null ||

                        this.status.isBlank()
        ) {

            this.status = "OPEN";
        }

        // =====================================================
        // USERNAME FALLBACKS
        // =====================================================

        if (

                this.applicantName == null &&

                        this.userName != null
        ) {

            this.applicantName =
                    this.userName;
        }

        if (

                this.name == null &&

                        this.userName != null
        ) {

            this.name =
                    this.userName;
        }

        if (

                this.email == null &&

                        this.userEmail != null
        ) {

            this.email =
                    this.userEmail;
        }
    }
}