package in.bawvpl.Authify.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import in.bawvpl.Authify.entity.AdminRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "users",

        indexes = {

                @Index(
                        name = "idx_user_email",
                        columnList = "email"
                ),

                @Index(
                        name = "idx_user_userid",
                        columnList = "user_id"
                ),

                @Index(
                        name = "idx_user_phone",
                        columnList = "phone_number"
                ),

                @Index(
                        name = "idx_user_role",
                        columnList = "admin_role"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {
        "password",
        "verificationToken",
        "resetOtp",
        "phoneOtp",
        "emailChangeOtp",
        "twoFactorSecret"
})
@JsonIgnoreProperties({
        "hibernateLazyInitializer",
        "handler"
})
public class UserEntity {

    // =====================================================
    // BASE URL
    // =====================================================

    private static final String BASE_URL =
            "http://43.205.116.38:8080";

    // =====================================================
    // ID
    // =====================================================

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;

    // =====================================================
    // USER ID
    // =====================================================

    @Builder.Default
    @Column(
            name = "user_id",
            unique = true,
            nullable = false,
            updatable = false,
            length = 20
    )
    private String userId = null;

    // =====================================================
    // ENTITY ID
    // =====================================================

    @Column(
            name = "entity_id",
            unique = true
    )
    private Long entityId;

    // =====================================================
    // BASIC DETAILS
    // =====================================================

    @Column(
            name = "entity_type",
            length = 50
    )
    private String entityType;

    @Column(
            name = "entity_name",
            length = 150
    )
    private String entityName;

    @Column(
            name = "contact_person",
            length = 100
    )
    private String contactPerson;

    @Column(
            nullable = false,
            unique = true,
            length = 150
    )
    private String email;

    @Column(
            name = "phone_number",
            length = 20
    )
    private String phoneNumber;

    // =====================================================
    // PASSWORD
    // =====================================================

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    // =====================================================
    // ROLE
    // =====================================================

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(
            name = "admin_role",
            nullable = false,
            length = 30
    )
    private AdminRole adminRole =
            AdminRole.ROLE_USER;

    // =====================================================
    // USER STATUS
    // =====================================================

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(
            name = "user_status",
            nullable = false,
            length = 30
    )
    private UserStatus userStatus =
            UserStatus.ACTIVE;

    // =====================================================
    // EMAIL VERIFICATION
    // =====================================================

    @Builder.Default
    @Column(
            name = "email_verified",
            nullable = false
    )
    private Boolean emailVerified = false;

    @JsonIgnore
    @Column(name = "verification_token")
    private String verificationToken;

    @Column(name = "verification_token_expiry")
    private LocalDateTime verificationTokenExpiry;

    // =====================================================
    // EMAIL CHANGE
    // =====================================================

    @Column(name = "pending_email")
    private String pendingEmail;

    @JsonIgnore
    @Column(name = "email_change_otp")
    private String emailChangeOtp;

    @Column(name = "email_change_expiry")
    private LocalDateTime emailChangeExpiry;

    // =====================================================
    // PHONE VERIFICATION
    // =====================================================

    @Builder.Default
    @Column(
            name = "phone_verified",
            nullable = false
    )
    private Boolean phoneVerified = false;

    @JsonIgnore
    @Column(name = "phone_otp")
    private String phoneOtp;

    @Column(name = "phone_otp_expiry")
    private LocalDateTime phoneOtpExpiry;

    // =====================================================
    // KYC
    // =====================================================

    @Builder.Default
    @Column(
            name = "is_kyc_verified",
            nullable = false
    )
    private Boolean isKycVerified = false;

    @Builder.Default
    @Column(
            name = "kyc_status",
            length = 30
    )
    private String kycStatus = "PENDING";

    // =====================================================
    // ADDRESS
    // =====================================================

    @Column(length = 500)
    private String address;

    // =====================================================
    // REFERRAL
    // =====================================================

    @Builder.Default
    @Column(
            name = "referral_code",
            unique = true,
            length = 20
    )
    private String referralCode = null;

    @Column(
            name = "referred_by",
            nullable = true
    )
    private String referredBy;

    // =====================================================
    // PROFILE PHOTO
    // =====================================================

    @Column(name = "photo_url")
    private String photoUrl;

    // =====================================================
    // JWT TOKEN VERSION
    // =====================================================

    @Builder.Default
    @Column(
            name = "token_version",
            nullable = false
    )
    private Integer tokenVersion = 0;

    // =====================================================
    // REFRESH TOKEN
    // =====================================================

    @JsonIgnore
    @Column(
            name = "refresh_token",
            length = 1000
    )
    private String refreshToken;

    // =====================================================
    // 2FA
    // =====================================================

    @Builder.Default
    @Column(
            name = "two_factor_enabled",
            nullable = false
    )
    private Boolean twoFactorEnabled = false;

    @JsonIgnore
    @Column(name = "two_factor_secret")
    private String twoFactorSecret;

    // =====================================================
    // RESET PASSWORD
    // =====================================================

    @JsonIgnore
    @Column(name = "reset_otp")
    private String resetOtp;

    @Column(name = "reset_otp_expiry")
    private LocalDateTime resetOtpExpiry;

    // =====================================================
    // LAST LOGIN
    // =====================================================

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    // =====================================================
    // TIMESTAMPS
    // =====================================================

    @Builder.Default
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt =
            LocalDateTime.now();

    @Builder.Default
    @Column(name = "updated_at")
    private LocalDateTime updatedAt =
            LocalDateTime.now();

    @Column(length = 150)
    private String companyName;

    @Column(length = 100)
    private String designation;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(length = 100)
    private String country;

    @Column(length = 20)
    private String postalCode;

    // =====================================================
    // ENTITY LIFECYCLE
    // =====================================================

    @PrePersist
    protected void prePersist() {

        LocalDateTime now =
                LocalDateTime.now();

        applyDefaults();

        normalizeFields();

        if (

                this.userId == null ||

                        this.userId.isBlank()
        ) {

            this.userId =
                    generateUserId();
        }

        if (

                this.referralCode == null ||

                        this.referralCode.isBlank()
        ) {

            this.referralCode =
                    generateReferralCode();
        }

        if (this.createdAt == null) {

            this.createdAt = now;
        }

        this.updatedAt = now;
    }

    @PreUpdate
    protected void preUpdate() {

        applyDefaults();

        normalizeFields();

        this.updatedAt =
                LocalDateTime.now();
    }

    // =====================================================
    // DEFAULTS
    // =====================================================

    private void applyDefaults() {

        if (this.adminRole == null) {

            this.adminRole = AdminRole.ROLE_USER;
        }

        if (this.userStatus == null) {

            this.userStatus = UserStatus.ACTIVE;
        }

        if (this.emailVerified == null) {

            this.emailVerified = false;
        }

        if (this.phoneVerified == null) {

            this.phoneVerified = false;
        }

        if (this.isKycVerified == null) {

            this.isKycVerified = false;
        }

        if (this.twoFactorEnabled == null) {

            this.twoFactorEnabled = false;
        }

        if (this.tokenVersion == null) {

            this.tokenVersion = 0;
        }
    }

    // =====================================================
    // NORMALIZATION
    // =====================================================

    private void normalizeFields() {

        if (this.email != null) {

            this.email =
                    this.email
                            .trim()
                            .toLowerCase();
        }

        if (this.pendingEmail != null) {

            this.pendingEmail =
                    this.pendingEmail
                            .trim()
                            .toLowerCase();
        }

        if (this.phoneNumber != null) {

            this.phoneNumber =
                    this.phoneNumber.trim();
        }

        if (this.entityName != null) {

            this.entityName =
                    this.entityName.trim();
        }

        if (this.contactPerson != null) {

            this.contactPerson =
                    this.contactPerson.trim();
        }

        if (this.address != null) {

            this.address =
                    this.address.trim();
        }

        if (this.photoUrl != null) {

            this.photoUrl =
                    normalizeUrl(
                            this.photoUrl
                    );
        }

        if (this.adminRole == null) {

            this.adminRole = AdminRole.ROLE_USER;
        }

        if(companyName!=null)
            companyName=companyName.trim();

        if(designation!=null)
            designation=designation.trim();

        if(city!=null)
            city=city.trim();

        if(state!=null)
            state=state.trim();

        if(country!=null)
            country=country.trim();

        if(postalCode!=null)
            postalCode=postalCode.trim();

    } // <-- CLOSE normalizeFields()




    // =====================================================
    // URL NORMALIZER
    // =====================================================

    private String normalizeUrl(
            String value
    ) {

        if (

                value == null ||

                        value.isBlank()
        ) {

            return null;
        }

        value = value.trim();

        // =====================================================
        // BASE64 IMAGE SUPPORT
        // =====================================================

        if (

                value.startsWith("data:image")
        ) {

            return value;
        }

        // =====================================================
        // FULL URL SUPPORT
        // =====================================================

        if (

                value.startsWith("http://") ||

                        value.startsWith("https://")
        ) {

            return value;
        }

        // =====================================================
        // STATIC FILE PATH SUPPORT
        // =====================================================

        if (!value.startsWith("/")) {

            value = "/" + value;
        }

        return BASE_URL + value;
    }
    // =====================================================
    // GENERATORS
    // =====================================================

    private String generateUserId() {

        String prefix = "USR-";

        if (this.entityType != null
                && this.entityType.equalsIgnoreCase("ORGANIZATION")) {

            prefix = "ORG-";
        }

        return prefix +

                UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 8)
                        .toUpperCase();
    }

    private String generateReferralCode() {

        return "REF" +

                UUID.randomUUID()
                        .toString()
                        .substring(0, 8)
                        .toUpperCase();
    }

    // =====================================================
    // ROLE HELPERS
    // =====================================================

    public boolean isAdmin() {

        return this.adminRole == AdminRole.ROLE_ADMIN;
    }

    public boolean isOwner() {

        return this.adminRole == AdminRole.ROLE_OWNER;
    }

    public boolean isAdminOrOwner() {

        return isAdmin() || isOwner();
    }

    public boolean isUser() {

        return this.adminRole == AdminRole.ROLE_USER;
    }

    public boolean isPrivilegedRole() {

        return isAdmin() || isOwner();
    }

    // =====================================================
    // ROLE COMPATIBILITY
    // =====================================================

    public String getRole() {

        return this.adminRole.name();
    }

    public void setRole(String role) {

        if (role == null || role.isBlank()) {

            this.adminRole = AdminRole.ROLE_USER;
            return;
        }

        role = role.trim().toUpperCase();

        if (!role.startsWith("ROLE_")) {

            role = "ROLE_" + role;
        }

        this.adminRole = AdminRole.valueOf(role);
    }

    // =====================================================
    // STATUS HELPERS
    // =====================================================

    public boolean isBlocked() {

        return this.userStatus ==
                UserStatus.BLOCKED;
    }

    public boolean isActive() {

        return this.userStatus ==
                UserStatus.ACTIVE;
    }

    public boolean isSuspended() {

        return this.userStatus ==
                UserStatus.SUSPENDED;
    }

    public boolean isDeleted() {

        return this.userStatus ==
                UserStatus.DELETED;
    }

    // =====================================================
    // SECURITY HELPERS
    // =====================================================

    public boolean canLogin() {

        return !isBlocked()

                &&

                !isSuspended()

                &&

                !isDeleted();
    }

    public boolean isFullyVerified() {

        return Boolean.TRUE.equals(
                this.emailVerified
        );
    }

    // =====================================================
    // DISPLAY HELPERS
    // =====================================================

    public String getDisplayName() {

        if (

                this.entityName != null &&

                        !this.entityName.isBlank()
        ) {

            return this.entityName;
        }

        if (

                this.contactPerson != null &&

                        !this.contactPerson.isBlank()
        ) {

            return this.contactPerson;
        }

        return this.email;
    }

    public String getSafePhotoUrl() {

        return normalizeUrl(
                this.photoUrl
        );
    }

    // =====================================================
    // LOGIN TRACKING
    // =====================================================

    public void markLoginSuccess() {

        this.lastLoginAt =
                LocalDateTime.now();
    }

    // =====================================================
    // TOKEN VERSION
    // =====================================================

    public void incrementTokenVersion() {

        if (this.tokenVersion == null) {

            this.tokenVersion = 0;
        }

        this.tokenVersion++;
    }
}
