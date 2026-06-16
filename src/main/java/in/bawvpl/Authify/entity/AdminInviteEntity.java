package in.bawvpl.Authify.entity;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "admin_invites"
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminInviteEntity {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;

    @Column(
            nullable = false,
            length = 255
    )
    private String email;

    @Column(
            name = "full_name",
            nullable = false,
            length = 255
    )
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 20
    )
    private AdminRole role;

    @Column(
            name = "token_hash",
            nullable = false,
            unique = true,
            length = 64
    )
    private String tokenHash;

    @Column(
            name = "invited_by",
            nullable = false
    )
    private Long invitedBy;

    @Column(
            name = "expires_at",
            nullable = false
    )
    private LocalDateTime expiresAt;

    @Builder.Default
    @Column(
            nullable = false
    )
    private Boolean used = false;

    @Builder.Default
    @Column(
            name = "created_at",
            nullable = false
    )
    private LocalDateTime createdAt =
            LocalDateTime.now();
}