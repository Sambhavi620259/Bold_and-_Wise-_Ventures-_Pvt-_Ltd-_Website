package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.KycEntity;
import in.bawvpl.Authify.entity.KycStatus;
import in.bawvpl.Authify.entity.UserEntity;

import in.bawvpl.Authify.io.ProfileRequest;
import in.bawvpl.Authify.io.ProfileResponse;

import in.bawvpl.Authify.repository.KycRepository;
import in.bawvpl.Authify.repository.UserRepository;

import in.bawvpl.Authify.entity.UserProfileHistory;
import in.bawvpl.Authify.repository.UserProfileHistoryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;

    private final UserProfileHistoryRepository userProfileHistoryRepository;

    private final PasswordEncoder passwordEncoder;

    private final EmailService emailService;

    private final KycRepository kycRepository;

    private final OtpService otpService;

    private final S3Service s3Service;

    private final SmsService smsService;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    // =====================================================
    // CREATE PROFILE
    // =====================================================

    @Override
    @Transactional
    public ProfileResponse createProfile(
            ProfileRequest request
    ) {

        if (request != null) {
            request.normalize();
        }

        if (!isValidProfile(request)) {

            throw new ResponseStatusException(

                    HttpStatus.BAD_REQUEST,

                    "Invalid profile request"
            );
        }

        String email =
                safe(request.getEmail())
                        .toLowerCase();

        // =====================================================
        // EMAIL EXISTS
        // =====================================================

        if (

                userRepository.existsByEmailIgnoreCase(
                        email
                )
        ) {

            throw new ResponseStatusException(

                    HttpStatus.CONFLICT,

                    "Email already exists"
            );
        }

        // =====================================================
        // TOKEN
        // =====================================================

        String verificationToken =
                UUID.randomUUID().toString();

        LocalDateTime verificationExpiry =
                LocalDateTime.now()
                        .plusHours(24);

        // =====================================================
        // ENTITY ID
        // =====================================================

        Long entityId =

                userRepository
                        .findTopByOrderByEntityIdDesc()

                        .map(lastUser ->
                                lastUser.getEntityId() + 1
                        )

                        .orElse(1000001L);

        // =====================================================
        // PHOTO URL
        // =====================================================

        String photoUrl =

                request.getPhotoUrl() != null

                        ? request.getPhotoUrl()

                        : request.getProfilePhotoUrl();

        if (

                photoUrl == null &&

                        request.getAvatarUrl() != null
        ) {

            photoUrl =
                    request.getAvatarUrl();
        }

        // =====================================================
        // USER
        // =====================================================

        UserEntity user =
                UserEntity.builder()

                        .userId(

                                "USR-" +

                                        UUID.randomUUID()
                                                .toString()
                                                .substring(0, 8)
                                                .toUpperCase()
                        )

                        .entityId(
                                entityId
                        )

                        .entityName(
                                safe(request.getName())
                        )

                        .contactPerson(
                                safe(request.getName())
                        )

                        .email(email)

                        .phoneNumber(
                                safe(
                                        request.getPhoneNumber()
                                )
                        )

                        .password(

                                passwordEncoder.encode(

                                        request.getPassword() == null

                                                ? UUID.randomUUID().toString()

                                                : request.getPassword()
                                )
                        )

                        // =====================================================
                        // FIXED
                        // =====================================================

                        .adminRole("ROLE_USER")

                        .entityType("INDIVIDUAL")

                        .address(
                                safe(
                                        request.getAddress()
                                )
                        )

                        .photoUrl(
                                normalizePhotoUrl(photoUrl)
                        )

                        .referralCode(
                                generateReferralCode(null)
                        )

                        .emailVerified(false)

                        .phoneVerified(false)

                        .isKycVerified(false)

                        .verificationToken(
                                verificationToken
                        )

                        .verificationTokenExpiry(
                                verificationExpiry
                        )

                        .createdAt(
                                LocalDateTime.now()
                        )

                        .updatedAt(
                                LocalDateTime.now()
                        )

                        .build();
        if (
                request.getReferralCode() != null &&
                        !request.getReferralCode().isBlank()
        ) {

            Optional<UserEntity> refUser =
                    userRepository.findByReferralCode(
                            request.getReferralCode().trim()
                    );

            if (refUser.isPresent()) {

                user.setReferredBy(
                        String.valueOf(
                                refUser.get().getEntityId()
                        )
                );

            } else {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid referral code"
                );
            }
        }

        user =
                userRepository.save(user);

        // =====================================================
        // KYC
        // =====================================================

        KycEntity kyc =
                new KycEntity();

        kyc.setUser(user);

        kyc.setDocumentType(
                safe(request.getDocumentType())
        );

        kyc.setDocumentNumber(
                safe(request.getDocumentNumber())
        );

        kyc.setFilePath(
                safe(request.getFilePath())
        );

        kyc.setStatus(
                KycStatus.PENDING
        );

        kyc.setCompleted(false);

        kycRepository.save(kyc);

        // =====================================================
        // EMAIL
        // =====================================================

        try {

            String verificationLink =

                    "http://43.205.116.38:8080/api/v1.0/verify-email?token="
                            + verificationToken;

            emailService.sendVerificationEmail(

                    user.getEmail(),

                    verificationLink
            );

        } catch (Exception e) {

            log.error(
                    "Verification email failed",
                    e
            );
        }

        return convert(user);
    }

    // =====================================================
    // GET PROFILE
    // =====================================================

    @Override
    public ProfileResponse getProfile(
            String email
    ) {

        return convert(
                find(email)
        );
    }

    // =====================================================
    // UPDATE PROFILE
    // =====================================================

    @Override
    @Transactional
    public ProfileResponse updateProfile(

            String email,

            ProfileRequest request
    ) {

        UserEntity user =
                find(email);

        String oldEmail = user.getEmail();
        String oldPhone = user.getPhoneNumber();

        if (request != null) {
            request.normalize();
        }

        if (

                request.getName() != null &&

                        !request.getName().isBlank()
        ) {

            user.setEntityName(
                    safe(request.getName())
            );

            user.setContactPerson(
                    safe(request.getName())
            );
        }

        if (

                request.getPhoneNumber() != null &&

                        !request.getPhoneNumber().isBlank()
        ) {

            user.setPhoneNumber(
                    safe(request.getPhoneNumber())
            );
        }

        if (

                request.getAddress() != null &&

                        !request.getAddress().isBlank()
        ) {

            user.setAddress(
                    safe(request.getAddress())
            );
        }

        // =====================================================
        // PHOTO
        // =====================================================

        String photoUrl =

                request.getPhotoUrl() != null

                        ? request.getPhotoUrl()

                        : request.getProfilePhotoUrl();

        if (

                photoUrl == null &&

                        request.getAvatarUrl() != null
        ) {

            photoUrl =
                    request.getAvatarUrl();
        }

        if (

                photoUrl != null &&

                        !photoUrl.isBlank()
        ) {

            user.setPhotoUrl(
                    normalizePhotoUrl(photoUrl)
            );
        }

        user.setUpdatedAt(
                LocalDateTime.now()
        );

        user =
                userRepository.save(user);

        // ==========================================
        // PROFILE HISTORY
        // ==========================================

        boolean emailChanged =
                oldEmail != null &&
                        !oldEmail.equals(user.getEmail());

        boolean phoneChanged =
                oldPhone != null &&
                        !oldPhone.equals(user.getPhoneNumber());

        if (emailChanged || phoneChanged) {

            UserProfileHistory history =
                    UserProfileHistory.builder()

                            .userId(
                                    user.getUserId()
                            )

                            .oldEmail(
                                    oldEmail
                            )

                            .newEmail(
                                    user.getEmail()
                            )

                            .oldPhone(
                                    oldPhone
                            )

                            .newPhone(
                                    user.getPhoneNumber()
                            )

                            .changedBy(
                                    user.getUserId()
                            )

                            .changedAt(
                                    LocalDateTime.now()
                            )

                            .build();

            userProfileHistoryRepository.save(
                    history
            );
        }

        return convert(user);
    }

    // =====================================================
    // UPDATE PHOTO
    // =====================================================

    @Override
    @Transactional
    public String updateProfilePhoto(

            String email,

            String photoUrl
    ) {

        UserEntity user =
                find(email);

        user.setPhotoUrl(
                normalizePhotoUrl(photoUrl)
        );

        user.setUpdatedAt(
                LocalDateTime.now()
        );

        userRepository.save(user);

        return user.getPhotoUrl();
    }

    // =====================================================
    // VERIFY EMAIL
    // =====================================================

    @Override
    @Transactional
    public void verifyEmailToken(
            String token
    ) {

        UserEntity user =

                userRepository
                        .findByVerificationToken(token)
                        .orElseThrow(() ->

                                new ResponseStatusException(

                                        HttpStatus.BAD_REQUEST,

                                        "Invalid verification token"
                                )
                        );

        if (

                user.getVerificationTokenExpiry() == null ||

                        user.getVerificationTokenExpiry()
                                .isBefore(LocalDateTime.now())
        ) {

            throw new ResponseStatusException(

                    HttpStatus.BAD_REQUEST,

                    "Verification link expired"
            );
        }

        user.setEmailVerified(true);

        user.setVerificationToken(null);

        user.setVerificationTokenExpiry(null);

        user.setUpdatedAt(
                LocalDateTime.now()
        );

        userRepository.save(user);
    }

    // =====================================================
    // NORMALIZE PHOTO URL
    // =====================================================

    @Override
    public String normalizePhotoUrl(
            String url
    ) {

        if (

                url == null ||

                        url.isBlank()
        ) {

            return null;
        }

        url = url.trim();

        if (

                url.startsWith("http://") ||

                        url.startsWith("https://")
        ) {

            return url;
        }

        if (!url.startsWith("/")) {

            url = "/" + url;
        }

        return "http://43.205.116.38:8080" + url;
    }

    // =====================================================
    // CONVERT
    // =====================================================

    private ProfileResponse convert(
            UserEntity user
    ) {

        Optional<KycEntity> kycOpt =
                kycRepository.findByUser(user);

        String kycStatus =

                kycOpt
                        .map(k ->
                                k.getStatus().name()
                        )
                        .orElse("NOT_SUBMITTED");

        boolean kycVerified =

                kycOpt
                        .map(k ->

                                k.getStatus() == KycStatus.VERIFIED ||

                                        k.getStatus() == KycStatus.APPROVED
                        )
                        .orElse(false);

        String photoUrl =
                normalizePhotoUrl(
                        user.getPhotoUrl()
                );

        ProfileResponse response =

                ProfileResponse.builder()

                        .userId(
                                user.getUserId()
                        )

                        .name(
                                user.getEntityName()
                        )

                        .fullName(
                                user.getEntityName()
                        )

                        .email(
                                user.getEmail()
                        )

                        .phoneNumber(
                                user.getPhoneNumber()
                        )

                        .emailVerified(
                                Boolean.TRUE.equals(
                                        user.getEmailVerified()
                                )
                        )

                        .phoneVerified(
                                Boolean.TRUE.equals(
                                        user.getPhoneVerified()
                                )
                        )

                        .accountVerified(
                                Boolean.TRUE.equals(
                                        user.getEmailVerified()
                                )
                        )

                        .kycVerified(
                                kycVerified
                        )

                        .kycStatus(
                                kycStatus
                        )

                        .referralCode(
                                user.getReferralCode()
                        )

                        .photoUrl(
                                photoUrl
                        )

                        .profilePhotoUrl(
                                photoUrl
                        )

                        .avatarUrl(
                                photoUrl
                        )

                        .kyc(

                                ProfileResponse.Kyc.builder()

                                        .status(
                                                kycStatus
                                        )

                                        .documentType(

                                                kycOpt
                                                        .map(KycEntity::getDocumentType)
                                                        .orElse(null)
                                        )

                                        .documentNumber(

                                                kycOpt
                                                        .map(KycEntity::getDocumentNumber)
                                                        .orElse(null)
                                        )

                                        .filePath(

                                                kycOpt
                                                        .map(KycEntity::getFilePath)
                                                        .orElse(null)
                                        )

                                        .rejectionReason(

                                                kycOpt
                                                        .map(KycEntity::getRejectionReason)
                                                        .orElse(null)
                                        )

                                        .build()
                        )

                        .build();

        response.normalize();

        return response;
    }

    // =====================================================
    // HELPERS
    // =====================================================

    private UserEntity find(
            String email
    ) {

        return userRepository
                .findByEmailIgnoreCase(
                        safe(email).toLowerCase()
                )
                .orElseThrow(() ->

                        new ResponseStatusException(

                                HttpStatus.NOT_FOUND,

                                "User not found"
                        )
                );
    }

    private String generateReferralCode(
            String referralCode
    ) {

        String code =
                referralCode;

        if (

                code == null ||

                        code.isBlank()
        ) {

            code =

                    "REF-" +

                            UUID.randomUUID()
                                    .toString()
                                    .substring(0, 8)
                                    .toUpperCase();
        }

        while (

                userRepository.existsByReferralCode(
                        code
                )
        ) {

            code =

                    "REF-" +

                            UUID.randomUUID()
                                    .toString()
                                    .substring(0, 8)
                                    .toUpperCase();
        }

        return code;
    }

    // =====================================================
    // REQUIRED METHODS
    // =====================================================

    @Override
    public String getLoggedInUserId(
            String email
    ) {

        return find(email)
                .getUserId();
    }

    @Override
    public boolean existsByEmail(
            String email
    ) {

        return userRepository.existsByEmailIgnoreCase(
                safe(email).toLowerCase()
        );
    }

    @Override
    public UserEntity findByEmail(
            String email
    ) {

        return find(email);
    }

    @Override
    public UserEntity save(
            UserEntity userEntity
    ) {

        return userRepository.save(userEntity);
    }

    @Override
    @Transactional
    public Map<String, Object> reuploadKyc(

            String email,

            String documentType,

            String documentNumber,

            MultipartFile file
    ) {

        UserEntity user =
                find(email);

        KycEntity kyc =
                kycRepository
                        .findByUser(user)
                        .orElseThrow(() ->

                                new ResponseStatusException(

                                        HttpStatus.NOT_FOUND,

                                        "KYC record not found"
                                )
                        );

        if (kyc.getStatus() != KycStatus.REJECTED) {

            throw new ResponseStatusException(

                    HttpStatus.BAD_REQUEST,

                    "Only rejected KYC can be reuploaded"
            );
        }

        try {

            String newFilePath =
                    s3Service.uploadFile(file);

            kyc.setDocumentType(
                    documentType
            );

            kyc.setDocumentNumber(
                    documentNumber
            );

            kyc.setFilePath(
                    newFilePath
            );

            kyc.setStatus(
                    KycStatus.PENDING
            );

            kyc.setRejectionReason(
                    null
            );

            kyc.setCompleted(
                    false
            );

            kycRepository.save(kyc);

            Map<String, Object> response =
                    new HashMap<>();

            response.put(
                    "success",
                    true
            );

            response.put(
                    "message",
                    "KYC resubmitted successfully"
            );

            return response;

        } catch (Exception e) {

            throw new ResponseStatusException(

                    HttpStatus.INTERNAL_SERVER_ERROR,

                    "KYC upload failed"
            );
        }
    }

    @Override
    public String getKycRejectionReason(
            String email
    ) {

        UserEntity user =
                find(email);

        return kycRepository
                .findByUser(user)
                .map(KycEntity::getRejectionReason)
                .orElse(null);
    }

    @Override
    public String getKycStatus(
            String email
    ) {

        UserEntity user =
                find(email);

        return kycRepository
                .findByUser(user)
                .map(k ->
                        k.getStatus().name()
                )
                .orElse("NOT_SUBMITTED");
    }

    @Override
    public Boolean isKycVerified(
            String email
    ) {

        UserEntity user =
                find(email);

        return kycRepository
                .findByUser(user)
                .map(k ->

                        k.getStatus() == KycStatus.VERIFIED ||

                                k.getStatus() == KycStatus.APPROVED
                )
                .orElse(false);
    }

    @Override
    public String getLastLogin(
            String email
    ) {

        UserEntity user =
                find(email);

        return user.getLastLoginAt() != null

                ? user.getLastLoginAt().toString()

                : (
                user.getUpdatedAt() != null

                        ? user.getUpdatedAt().toString()

                        : null
        );
    }

    // =====================================================
    // OPTIONAL EMPTY METHODS
    // =====================================================

    @Override
    public void sendVerificationOtp(String email) {
    }

    @Override
    public void verifyEmailOtp(String email, String otp) {
    }

    @Override
    public void sendResetOtp(String email) {
    }

    @Override
    public void resetPassword(String email, String otp, String newPassword) {
    }

    @Override
    public void requestEmailChange(String currentEmail, String newEmail) {
    }

    @Override
    public void verifyEmailChangeOtp(String email, String otp) {
    }

    @Override
    public void resendEmailChangeOtp(String email) {
    }

    @Override
    public void sendPhoneOtp(String email, String phoneNumber) {
    }

    @Override
    public void verifyPhoneOtp(String email, String otp) {
    }

    @Override
    public void initiateContactUpdate(String email, String newEmail, String newPhone) {
    }

    @Override
    public void verifyContactUpdate(String email, String otp, String newEmail, String newPhone) {
    }
}