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
public class NotificationResponse {

    // =====================================================
    // ID
    // =====================================================

    private Long id;

    // =====================================================
    // TITLE
    // =====================================================

    private String title;

    // =====================================================
    // MESSAGE
    // =====================================================

    private String message;

    // =====================================================
    // FRONTEND BODY ALIAS
    // =====================================================

    private String body;

    // =====================================================
    // TYPE
    // =====================================================

    @Builder.Default
    private String type = "INFO";

    // =====================================================
    // READ
    // =====================================================

    @Builder.Default
    private Boolean read = false;

    // =====================================================
    // FRONTEND READ ALIAS
    // =====================================================

    @Builder.Default
    private Boolean isRead = false;

    // =====================================================
    // READ AT
    // =====================================================

    private LocalDateTime readAt;

    // =====================================================
    // TIMESTAMP
    // =====================================================

    private LocalDateTime createdAt;

    // =====================================================
    // UPDATED
    // =====================================================

    private LocalDateTime updatedAt;

    // =====================================================
    // NORMALIZATION
    // =====================================================

    public void normalize() {

        // =====================================================
        // TITLE
        // =====================================================

        if (this.title != null) {

            this.title =
                    this.title.trim();
        }

        // =====================================================
        // MESSAGE
        // =====================================================

        if (this.message != null) {

            this.message =
                    this.message.trim();
        }

        // =====================================================
        // BODY FALLBACK
        // =====================================================

        if (

                (this.body == null ||

                        this.body.isBlank()) &&

                        this.message != null
        ) {

            this.body =
                    this.message;
        }

        // =====================================================
        // TYPE DEFAULT
        // =====================================================

        if (

                this.type == null ||

                        this.type.isBlank()
        ) {

            this.type = "INFO";
        }

        // =====================================================
        // SAFE READ DEFAULT
        // =====================================================

        if (this.read == null) {

            this.read = false;
        }

        // =====================================================
        // FRONTEND ALIAS
        // =====================================================

        this.isRead =
                this.read;
    }
}