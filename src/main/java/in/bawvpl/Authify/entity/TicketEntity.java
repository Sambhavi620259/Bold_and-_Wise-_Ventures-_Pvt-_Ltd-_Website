package in.bawvpl.Authify.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "tickets",
        indexes = {

                @Index(
                        name = "idx_ticket_user",
                        columnList = "user_id"
                ),

                @Index(
                        name = "idx_ticket_created",
                        columnList = "created_at"
                ),

                @Index(
                        name = "idx_ticket_status",
                        columnList = "status"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({
        "hibernateLazyInitializer",
        "handler"
})
public class TicketEntity {

    // =====================================================
    // ID
    // =====================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =====================================================
    // USER RELATION
    // =====================================================

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    @JsonIgnore
    private UserEntity user;

    // =====================================================
    // SUBJECT
    // =====================================================

    @Column(
            nullable = false,
            length = 200
    )
    private String subject;

    // =====================================================
    // STATUS
    // =====================================================

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(
            nullable = false,
            length = 30
    )
    private Status status = Status.OPEN;

    // =====================================================
    // MESSAGES
    // =====================================================

    @OneToMany(
            mappedBy = "ticket",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @OrderBy("createdAt ASC")
    @Builder.Default
    private List<TicketMessageEntity> messages =
            new ArrayList<>();

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
    // UPDATED AT
    // =====================================================

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // =====================================================
    // AUTO CREATE
    // =====================================================

    @PrePersist
    protected void onCreate() {

        LocalDateTime now =
                LocalDateTime.now();

        if (this.createdAt == null) {

            this.createdAt = now;
        }

        if (this.status == null) {

            this.status = Status.OPEN;
        }

        this.updatedAt = now;

        normalizeFields();
    }

    // =====================================================
    // AUTO UPDATE
    // =====================================================

    @PreUpdate
    protected void onUpdate() {

        this.updatedAt =
                LocalDateTime.now();

        normalizeFields();
    }

    // =====================================================
    // NORMALIZATION
    // =====================================================

    private void normalizeFields() {

        if (this.subject != null) {

            this.subject =
                    this.subject
                            .trim()
                            .replaceAll("\\s+", " ");
        }
    }
    // =====================================================
    // STATUS ENUM
    // =====================================================

    public enum Status {

        OPEN,

        PENDING,

        RESOLVED,

        CLOSED;

        public boolean isOpen() {

            return this == OPEN;
        }

        public boolean isPending() {

            return this == PENDING;
        }

        public boolean isResolved() {

            return this == RESOLVED;
        }

        public boolean isClosed() {

            return this == CLOSED;
        }
    }
}