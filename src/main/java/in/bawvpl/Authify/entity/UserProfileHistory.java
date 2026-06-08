package in.bawvpl.Authify.entity;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_profile_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "old_email")
    private String oldEmail;

    @Column(name = "new_email")
    private String newEmail;

    @Column(name = "old_phone")
    private String oldPhone;

    @Column(name = "new_phone")
    private String newPhone;

    @Column(name = "changed_by")
    private String changedBy;

    @Column(name = "changed_at")
    private LocalDateTime changedAt;
}