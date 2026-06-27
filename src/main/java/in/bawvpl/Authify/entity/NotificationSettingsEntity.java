package in.bawvpl.Authify.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notification_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationSettingsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserEntity user;

    @Builder.Default
    @Column(nullable = false)
    private Boolean pushNotifications = true;

    @Builder.Default
    @Column(nullable = false)
    private Boolean emailNotifications = true;

    @Builder.Default
    @Column(nullable = false)
    private Boolean smsNotifications = false;

    @Builder.Default
    @Column(nullable = false)
    private Boolean marketingEmails = false;

    @Builder.Default
    @Column(nullable = false)
    private Boolean securityAlerts = true;

    @Builder.Default
    @Column(nullable = false)
    private Boolean loginAlerts = true;

    @Builder.Default
    @Column(nullable = false)
    private Boolean newsletter = false;
}
