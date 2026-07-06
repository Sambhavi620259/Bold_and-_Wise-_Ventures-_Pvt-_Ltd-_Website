package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.UserEntity;
import in.bawvpl.Authify.entity.UserStatus;

import in.bawvpl.Authify.io.AdminRegisterRequest;

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
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminAuthService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final OtpService otpService;

    private final EmailService emailService;

    // =====================================================
    // ADMIN SECRET
    // =====================================================

    @Value("${admin.secret}")
    private String adminSecret;

    // =====================================================
    // REGISTER ADMIN
    // =====================================================

    @Transactional
    public void registerAdmin(
            AdminRegisterRequest request
    ) {

        // =====================================================
        // REQUEST VALIDATION
        // =====================================================

        if (request == null) {

            throw new ResponseStatusException(

                    HttpStatus.BAD_REQUEST,

                    "Request cannot be null"
            );
        }

        // =====================================================
        // NAME
        // =====================================================

        if (

                request.getName() == null ||

                        request.getName().isBlank()
        ) {

            throw new ResponseStatusException(

                    HttpStatus.BAD_REQUEST,

                    "Name required"
            );
        }

        // =====================================================
        // EMAIL
        // =====================================================

        if (

                request.getEmail() == null ||

                        request.getEmail().isBlank()
        ) {

            throw new ResponseStatusException(

                    HttpStatus.BAD_REQUEST,

                    "Email required"
            );
        }

        // =====================================================
        // PASSWORD
        // =====================================================

        if (

                request.getPassword() == null ||

                        request.getPassword().isBlank()
        ) {

            throw new ResponseStatusException(

                    HttpStatus.BAD_REQUEST,

                    "Password required"
            );
        }

        // =====================================================
        // ADMIN SECRET
        // =====================================================

        if (

                request.getAdminSecret() == null ||

                        request.getAdminSecret().isBlank()
        ) {

            throw new ResponseStatusException(

                    HttpStatus.BAD_REQUEST,

                    "Admin secret required"
            );
        }

        // =====================================================
        // SECRET VALIDATION
        // =====================================================

        if (

                adminSecret == null ||

                        adminSecret.isBlank()
        ) {

            throw new ResponseStatusException(

                    HttpStatus.INTERNAL_SERVER_ERROR,

                    "Admin secret not configured"
            );
        }

        if (

                !adminSecret.trim().equals(

                        request.getAdminSecret()
                                .trim()
                )
        ) {

            throw new ResponseStatusException(

                    HttpStatus.UNAUTHORIZED,

                    "Invalid admin secret"
            );
        }

        // =====================================================
        // NORMALIZE EMAIL
        // =====================================================

        String email =

                request.getEmail()
                        .trim()
                        .toLowerCase();

        // =====================================================
        // NORMALIZE PHONE
        // =====================================================

        String phone =

                request.getPhoneNumber() != null

                        ? request.getPhoneNumber()
                        .replaceAll("\\D", "")
                        .trim()

                        : "";

        // =====================================================
        // DUPLICATE EMAIL
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
        // DUPLICATE PHONE
        // =====================================================

        if (

                !phone.isBlank()

                        &&

                        userRepository.existsByPhoneNumber(
                                phone
                        )
        ) {

            throw new ResponseStatusException(

                    HttpStatus.CONFLICT,

                    "Phone number already exists"
            );
        }

        // =====================================================
        // ROLE
        // =====================================================

        String role = "ROLE_ADMIN";

        // =====================================================
        // CREATE USER
        // =====================================================

        UserEntity user =
                UserEntity.builder()

                        .userId(

                                "ADM-" +

                                        UUID.randomUUID()
                                                .toString()
                                                .substring(0, 8)
                                                .toUpperCase()
                        )

                        .entityType("ADMIN")

                        .entityName(
                                request.getName().trim()
                        )

                        .contactPerson(
                                request.getName().trim()
                        )

                        .email(email)

                        .password(

                                passwordEncoder.encode(
                                        request.getPassword()
                                )
                        )

                        .phoneNumber(phone)

                        // =====================================================
                        // ROLE FIX
                        // =====================================================

                        .adminRole(AdminRole.valueOf(role))

                        .emailVerified(true)

                        .phoneVerified(true)

                        .isKycVerified(true)

                        // =====================================================
                        // USER STATUS
                        // =====================================================

                        .userStatus(
                                UserStatus.ACTIVE
                        )

                        // =====================================================
                        // TOKEN VERSION FIX
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
        // SAVE
        // =====================================================

        userRepository.save(user);

        log.info(
                "Admin registered successfully: {}",
                email
        );
    }

    // =====================================================
    // ADMIN LOGIN
    // =====================================================

    public void loginAndSendOtp(

            String email,

            String password
    ) {

        // =====================================================
        // VALIDATION
        // =====================================================

        if (

                email == null ||

                        email.isBlank()
        ) {

            throw new ResponseStatusException(

                    HttpStatus.BAD_REQUEST,

                    "Email required"
            );
        }

        if (

                password == null ||

                        password.isBlank()
        ) {

            throw new ResponseStatusException(

                    HttpStatus.BAD_REQUEST,

                    "Password required"
            );
        }

        String normalizedEmail =
                email.trim()
                        .toLowerCase();

        // =====================================================
        // FIND USER
        // =====================================================

        UserEntity user =
                userRepository

                        .findByEmailIgnoreCase(
                                normalizedEmail
                        )

                        .orElseThrow(() ->

                                new ResponseStatusException(

                                        HttpStatus.UNAUTHORIZED,

                                        "Invalid email or password"
                                )
                        );

        // =====================================================
        // ROLE CHECK
        // =====================================================
        String role =
                user.getAdminRole() != null
                        ? user.getAdminRole().name()
                        : "";

        if (!role.equalsIgnoreCase("ROLE_ADMIN")
                && !role.equalsIgnoreCase("ROLE_OWNER")) {

            throw new ResponseStatusException(

                    HttpStatus.FORBIDDEN,

                    "Admin access denied"
            );
        }
        // =====================================================
        // STATUS CHECK
        // =====================================================

        if (

                user.getUserStatus() != null &&

                        user.getUserStatus()
                                != UserStatus.ACTIVE
        ) {

            throw new ResponseStatusException(

                    HttpStatus.FORBIDDEN,

                    "Admin account inactive"
            );
        }

        // =====================================================
        // PASSWORD
        // =====================================================

        String storedPassword =
                user.getPassword();

        if (

                storedPassword == null ||

                        storedPassword.isBlank()
        ) {

            throw new ResponseStatusException(

                    HttpStatus.UNAUTHORIZED,

                    "Invalid email or password"
            );
        }

        boolean passwordMatched;

        try {

            passwordMatched =
                    passwordEncoder.matches(

                            password,

                            storedPassword
                    );

        } catch (Exception e) {

            log.error(
                    "Password encoder failed",
                    e
            );

            throw new ResponseStatusException(

                    HttpStatus.UNAUTHORIZED,

                    "Invalid email or password"
            );
        }

        if (!passwordMatched) {

            log.warn(
                    "Invalid admin password: {}",
                    normalizedEmail
            );

            throw new ResponseStatusException(

                    HttpStatus.UNAUTHORIZED,

                    "Invalid email or password"
            );
        }

        // =====================================================
        // EMAIL VERIFIED
        // =====================================================

        if (

                !Boolean.TRUE.equals(
                        user.getEmailVerified()
                )
        ) {

            throw new ResponseStatusException(

                    HttpStatus.FORBIDDEN,

                    "Please verify your email first"
            );
        }

        // =====================================================
        // DEBUG LOGGING
        // =====================================================

        log.info(
                "ADMIN ROLE = {}",
                user.getAdminRole()
        );

        log.info(
                "TOKEN VERSION = {}",
                user.getTokenVersion()
        );

        // =====================================================
        // GENERATE OTP
        // =====================================================

        String otp =
                otpService.generateLoginOtp(user);

        // =====================================================
        // SEND OTP
        // =====================================================

        try {

            emailService.sendVerificationOtpEmail(

                    user.getEmail(),

                    otp
            );

        } catch (Exception e) {

            log.error(
                    "OTP email failed",
                    e
            );

            throw new ResponseStatusException(

                    HttpStatus.INTERNAL_SERVER_ERROR,

                    "Failed to send OTP"
            );
        }

        // =====================================================
        // LOGGING
        // =====================================================

        log.info(
                "ADMIN OTP for {} = {}",
                user.getEmail(),
                otp
        );

        log.info(
                "Admin OTP sent successfully to {}",
                normalizedEmail
        );
    }
}