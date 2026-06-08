package in.bawvpl.Authify.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "ticket_messages",
        indexes = {

                @Index(
                        name = "idx_ticket_msg_ticket",
                        columnList = "ticket_id"
                ),

                @Index(
                        name = "idx_ticket_msg_created",
                        columnList = "created_at"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketMessageEntity {

    // =====================================================
    // ID
    // =====================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =====================================================
    // TICKET RELATION
    // =====================================================

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "ticket_id",
            nullable = false
    )
    @JsonIgnore
    private TicketEntity ticket;

    // =====================================================
    // SENDER TYPE
    // USER / ADMIN
    // =====================================================

    @Column(
            name = "sender_type",
            nullable = false,
            length = 20
    )
    private String senderType;

    // =====================================================
    // MESSAGE
    // =====================================================

    @Column(
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String message;

    // =====================================================
    // CREATED AT
    // =====================================================

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    // =====================================================
    // AUTO CREATE
    // =====================================================

    @PrePersist
    protected void onCreate() {

        if (
                this.createdAt == null
        ) {

            this.createdAt =
                    LocalDateTime.now();
        }

        normalizeFields();
    }

    // =====================================================
    // NORMALIZATION
    // =====================================================

    private void normalizeFields() {

        if (
                this.senderType != null
        ) {

            this.senderType =
                    this.senderType
                            .trim()
                            .toUpperCase();
        }

        if (
                this.message != null
        ) {

            this.message =
                    this.message
                            .trim();
        }
    }

    // =====================================================
    // HELPERS
    // =====================================================

    public boolean isAdminMessage() {

        return "ADMIN"
                .equalsIgnoreCase(
                        this.senderType
                );
    }

    public boolean isUserMessage() {

        return "USER"
                .equalsIgnoreCase(
                        this.senderType
                );
    }
}