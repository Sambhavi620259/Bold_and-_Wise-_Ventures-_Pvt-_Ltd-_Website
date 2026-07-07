package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.UserEntity;
import in.bawvpl.Authify.entity.UserStatus;

import in.bawvpl.Authify.io.RegisterRequest;

import in.bawvpl.Authify.repository.UserRepository;
import in.bawvpl.Authify.entity.AdminRole;

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

@Service
@RequiredArgsConstructor
@Slf4j
public class RegisterService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final ReferralService referralService;

    private final EmailService emailService;

    // =====================================================
    // FRONTEND URL
    // =====================================================

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    // =====================================================
    // REGISTER USER
    // =====================================================

    @Transactional
    public UserEntity registerUser(
            RegisterRequest req
    ) {

        try {

            // =====================================================
            // VALIDATION
            // =====================================================

            if (req == null) {

                throw new ResponseStatusException(

                        HttpStatus.BAD_REQUEST,

                        "Request cannot be null"
                );
            }

            // =====================================================
            // NORMALIZE EMAIL
            // =====================================================

            String email =

                    req.getEmail() != null

                            ? req.getEmail()
                            .trim()
                            .toLowerCase()

                            : "";

            // =====================================================
            // NORMALIZE PHONE
            // =====================================================

            String phone =

                    req.getPhoneNumber() != null

                            ? req.getPhoneNumber()
                            .replaceAll("\\D", "")
                            .trim()

                            : "";

            // =====================================================
            // NORMALIZE NAME
            // =====================================================

            String name =

                    req.getName() != null

                            ? req.getName().trim()

                            : "";

            // =====================================================
            // NORMALIZE ENTITY TYPE
            // =====================================================

            String entityType =

                    req.getEntityType() != null

                            ? req.getEntityType()
                            .trim()
                            .toUpperCase()

                            : "";

            // =====================================================
            // REQUIRED FIELDS
            // =====================================================

            if (email.isBlank()) {

                throw new ResponseStatusException(

                        HttpStatus.BAD_REQUEST,

                        "Email is required"
                );
            }

            if (

                    req.getPassword() == null ||

                            req.getPassword().isBlank()
            ) {

                throw new ResponseStatusException(

                        HttpStatus.BAD_REQUEST,

                        "Password is required"
                );
            }

            if (phone.isBlank()) {

                throw new ResponseStatusException(

                        HttpStatus.BAD_REQUEST,

                        "Phone number is required"
                );
            }

            if (entityType.isBlank()) {

                throw new ResponseStatusException(

                        HttpStatus.BAD_REQUEST,

                        "Entity type is required"
                );
            }

            // =====================================================
            // EMAIL DUPLICATE
            // =====================================================

            if (

                    userRepository.existsByEmailIgnoreCase(
                            email
                    )
            ) {

                throw new ResponseStatusException(

                        HttpStatus.CONFLICT,

                        "Email already registered"
                );
            }

            // =====================================================
            // PHONE DUPLICATE
            // =====================================================

            if (

                    userRepository.existsByPhoneNumber(
                            phone
                    )
            ) {

                throw new ResponseStatusException(

                        HttpStatus.CONFLICT,

                        "Phone number already registered"
                );
            }

            // =====================================================
            // ROLE
            // =====================================================

            AdminRole role = AdminRole.ROLE_USER;
            // =====================================================
            // USER ID
            // =====================================================

            String prefix =
                    "ORGANIZATION".equalsIgnoreCase(entityType)
                            ? "ORG-"
                            : "USR-";

            String userId =
                    prefix +
                            UUID.randomUUID()
                                    .toString()
                                    .replace("-", "")
                                    .substring(0, 8)
                                    .toUpperCase();

            // =====================================================
            // REFERRAL CODE
            // =====================================================

            String referralCode = userId;

            // =====================================================
            // VERIFICATION TOKEN
            // =====================================================

            String verificationToken =

                    UUID.randomUUID()
                            .toString();

            LocalDateTime verificationExpiry =

                    LocalDateTime.now()
                            .plusHours(24);

            log.info(
                    "Generated verification token for {}",
                    email
            );

            // =====================================================
            // CREATE USER
            // =====================================================

            UserEntity user =

                    UserEntity.builder()

                            .userId(userId)

                            .entityType(entityType)

                            .entityName(name)

                            .contactPerson(name)

                            .email(email)

                            .phoneNumber(phone)

                            .password(

                                    passwordEncoder.encode(
                                            req.getPassword()
                                    )
                            )

                            // =====================================================
                            // ROLE FIX
                            // =====================================================

                            .adminRole(role)

                            .address(

                                    req.getAddress() != null

                                            ? req.getAddress()
                                            .trim()

                                            : ""
                            )

                            .referralCode(referralCode)

                            .verificationToken(
                                    verificationToken
                            )

                            .verificationTokenExpiry(
                                    verificationExpiry
                            )

                            // =====================================================
                            // EMAIL VERIFICATION
                            // =====================================================

                            .emailVerified(false)

                            .phoneVerified(false)

                            .isKycVerified(false)

                            // =====================================================
                            // USER STATUS
                            // =====================================================

                            .userStatus(
                                    UserStatus.PENDING
                            )

                            // =====================================================
                            // TOKEN VERSION
                            // =====================================================

                            .tokenVersion(0)

                            .createdAt(
                                    LocalDateTime.now()
                            )

                            .updatedAt(
                                    LocalDateTime.now()
                            )

                            .build();

            // =====================================================
            // ROLE NORMALIZATION
            // =====================================================

            user.setRole(
                    user.getAdminRole().name()
            );


            // =====================================================
            // REFERRAL
            // =====================================================

            String refCode = "BWVPL#26";

            if (req.getReferralCode() != null &&
                    !req.getReferralCode().isBlank()) {

                refCode = req.getReferralCode().trim();
            }

            Optional<UserEntity> refUser =
                    userRepository.findByReferralCode(refCode);

            if (refUser.isEmpty()) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid referral code"
                );
            }

            UserEntity referrer = refUser.get();

            if (referrer.getEmail().equalsIgnoreCase(email)) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Self referral not allowed"
                );
            }

            if (referrer.getPhoneNumber() != null &&
                    referrer.getPhoneNumber().equals(phone)) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Self referral not allowed"
                );
            }

            referralService.applyReferral(user, refCode);
            // =====================================================
            // SAVE USER
            // =====================================================

            UserEntity savedUser =
                    userRepository.save(user);

            log.info(
                    "User saved successfully: {}",
                    savedUser.getEmail()
            );

            // =====================================================
            // VERIFY LINK
            // =====================================================

            String verificationLink =

                    frontendUrl +

                            "/verify-email?token=" +

                            savedUser.getVerificationToken();

            log.info(
                    "Verification link generated for {}",
                    savedUser.getEmail()
            );

            // =====================================================
            // SEND EMAIL
            // =====================================================

            try {

                emailService.sendVerificationEmail(

                        savedUser.getEmail(),

                        verificationLink
                );

                log.info(
                        "Verification email sent to {}",
                        savedUser.getEmail()
                );

            } catch (Exception e) {

                log.error(
                        "Email sending failed",
                        e
                );

                throw new ResponseStatusException(

                        HttpStatus.INTERNAL_SERVER_ERROR,

                        "Unable to send verification email"
                );
            }

            // =====================================================
            // SUCCESS
            // =====================================================

            return savedUser;

        } catch (ResponseStatusException e) {

            throw e;

        } catch (Exception e) {

            log.error(
                    "Registration failed",
                    e
            );

            throw new ResponseStatusException(

                    HttpStatus.INTERNAL_SERVER_ERROR,

                    "Registration failed"
            );
        }
    }
}