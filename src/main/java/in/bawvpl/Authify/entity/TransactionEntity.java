package in.bawvpl.Authify.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "transactions",

        indexes = {

                @Index(
                        name = "idx_tx_user",
                        columnList = "user_id"
                ),

                @Index(
                        name = "idx_tx_payment_date",
                        columnList = "payment_date"
                ),

                @Index(
                        name = "idx_tx_status",
                        columnList = "status"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {
        "user",
        "app"
})
@JsonIgnoreProperties({
        "hibernateLazyInitializer",
        "handler"
})
public class TransactionEntity {

    // =====================================================
    // ID
    // =====================================================

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;

    // =====================================================
    // USER
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
    // APPLICATION
    // =====================================================

    @ManyToOne(
            fetch = FetchType.LAZY
    )
    @JoinColumn(name = "app_id")
    @JsonIgnore
    private ApplicationEntity app;

    // =====================================================
    // DETAILS
    // =====================================================

    @Column(
            name = "payment_description",
            length = 255
    )
    private String paymentDescription;

    @Column(
            name = "payment_method",
            length = 50
    )
    private String paymentMethod;

    @Column(
            name = "payment_source",
            length = 50
    )
    private String paymentSource;

    // =====================================================
    // AMOUNT
    // =====================================================

    @Builder.Default
    @Column(
            nullable = false
    )
    private Double amount = 0.0;

    // =====================================================
    // TYPE
    // CREDIT / DEBIT
    // =====================================================

    @Builder.Default
    @Column(
            name = "type",
            nullable = false,
            length = 20
    )
    private String type = "DEBIT";

    // =====================================================
    // STATUS
    // SUCCESS / FAILED / PENDING
    // =====================================================

    @Builder.Default
    @Column(
            name = "status",
            nullable = false,
            length = 20
    )
    private String status = "PENDING";

    // =====================================================
    // PAYMENT DATE
    // =====================================================

    @Column(
            name = "payment_date",
            nullable = false
    )
    private LocalDateTime paymentDate;

    // =====================================================
    // CREATED
    // =====================================================

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    // =====================================================
    // UPDATED
    // =====================================================

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // =====================================================
    // PRE PERSIST
    // =====================================================

    @PrePersist
    public void onCreate() {

        LocalDateTime now =
                LocalDateTime.now();

        // =====================================================
        // DEFAULTS
        // =====================================================

        if (this.paymentDate == null) {

            this.paymentDate = now;
        }

        if (this.createdAt == null) {

            this.createdAt = now;
        }

        this.updatedAt = now;

        if (this.amount == null) {

            this.amount = 0.0;
        }

        if (

                this.status == null ||

                        this.status.isBlank()
        ) {

            this.status = "PENDING";
        }

        if (

                this.type == null ||

                        this.type.isBlank()
        ) {

            this.type = "DEBIT";
        }

        // =====================================================
        // NORMALIZE
        // =====================================================

        normalizeFields();
    }

    // =====================================================
    // PRE UPDATE
    // =====================================================

    @PreUpdate
    public void onUpdate() {

        this.updatedAt =
                LocalDateTime.now();

        normalizeFields();
    }

    // =====================================================
    // NORMALIZATION
    // =====================================================

    private void normalizeFields() {

        if (this.paymentDescription != null) {

            this.paymentDescription =
                    this.paymentDescription.trim();
        }

        if (this.paymentMethod != null) {

            this.paymentMethod =
                    this.paymentMethod
                            .trim()
                            .toUpperCase();
        }

        if (this.paymentSource != null) {

            this.paymentSource =
                    this.paymentSource
                            .trim()
                            .toUpperCase();
        }

        if (this.status != null) {

            this.status =
                    this.status
                            .trim()
                            .toUpperCase();
        }

        if (this.type != null) {

            this.type =
                    this.type
                            .trim()
                            .toUpperCase();
        }
    }

    // =====================================================
    // HELPERS
    // =====================================================

    public boolean isSuccess() {

        return "SUCCESS"
                .equalsIgnoreCase(
                        this.status
                );
    }

    public boolean isFailed() {

        return "FAILED"
                .equalsIgnoreCase(
                        this.status
                );
    }

    public boolean isPending() {

        return "PENDING"
                .equalsIgnoreCase(
                        this.status
                );
    }

    public boolean isCredit() {

        return "CREDIT"
                .equalsIgnoreCase(
                        this.type
                );
    }

    public boolean isDebit() {

        return "DEBIT"
                .equalsIgnoreCase(
                        this.type
                );
    }
}