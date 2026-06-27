package in.bawvpl.Authify.io;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettingsResponse {

    // Notifications
    private Boolean notificationsEnabled;
    private Boolean emailAlerts;
    private Boolean darkMode;

    // Production notification settings
    private Boolean marketingEmails;
    private Boolean ticketUpdates;
    private Boolean paymentAlerts;
    private Boolean activityTracking;
    private Boolean recommendations;

    // Company/Profile
    private String companyName;
    private String designation;
    private String address;
    private String city;
    private String state;
    private String country;
    private String postalCode;
}