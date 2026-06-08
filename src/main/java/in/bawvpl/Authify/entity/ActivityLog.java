package in.bawvpl.Authify.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(
        name = "activity_logs",
        indexes = {

                @Index(
                        name = "idx_activity_user",
                        columnList = "user_id"
                ),

                @Index(
                        name = "idx_activity_timestamp",
                        columnList = "timestamp"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityLog {

    // =====================================================
    // ID
    // =====================================================

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
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
    // ACTION
    // =====================================================

    @Column(
            nullable = false,
            length = 100
    )
    private String action;

    // =====================================================
    // DESCRIPTION
    // =====================================================

    @Column(
            name = "description",
            length = 500
    )
    private String description;

    // =====================================================
    // TIMESTAMP
    // =====================================================

    @Column(
            nullable = false,
            updatable = false
    )
    private Instant timestamp;

    // =====================================================
    // AUTO TIMESTAMP
    // =====================================================

    @PrePersist
    protected void onCreate() {

        if (timestamp == null) {

            timestamp = Instant.now();
        }
    }
}