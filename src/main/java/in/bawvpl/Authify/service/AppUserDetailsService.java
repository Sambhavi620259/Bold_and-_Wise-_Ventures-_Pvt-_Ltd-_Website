package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.KycEntity;
import in.bawvpl.Authify.entity.UserEntity;

import in.bawvpl.Authify.io.AuthResponse;
import in.bawvpl.Authify.io.ProfileResponse;

import in.bawvpl.Authify.repository.KycRepository;
import in.bawvpl.Authify.repository.UserRepository;

import in.bawvpl.Authify.util.JwtUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cache.annotation.Cacheable;

import org.springframework.http.HttpStatus;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppUserDetailsService
        implements UserDetailsService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final EmailService emailService;

    private final OtpService otpService;

    private final JwtUtil jwtUtil;

    private final KycRepository kycRepository;

    // =====================================================
    // LOAD USER
    // =====================================================

    @Override
    @Cacheable(value = "users", key = "#username")
    public UserDetails loadUserByUsername(
            String username
    ) throws UsernameNotFoundException {

        try {

            if (

                    username == null ||

                            username.isBlank()
            ) {

                throw new UsernameNotFoundException(
                        "Username required"
                );
            }

            String email =
                    username.trim()
                            .toLowerCase();

            UserEntity user =
                    userRepository
                            .findByEmailIgnoreCase(email)

                            .orElseThrow(() ->

                                    new UsernameNotFoundException(
                                            "User not found"
                                    )
                            );

            // =====================================================
            // ROLE
            // =====================================================

            String role =

                    (user.getRole() != null &&

                            !user.getRole().isBlank())

                            ? user.getRole()

                            : "ROLE_USER";

            // =====================================================
            // FORCE ROLE PREFIX
            // =====================================================

            if (!role.startsWith("ROLE_")) {

                role = "ROLE_" + role;
            }

            log.debug(

                    "Loaded user {} with role {}",

                    user.getEmail(),

                    role
            );

            return new org.springframework.security.core.userdetails.User(

                    user.getEmail(),

                    user.getPassword(),

                    List.of(

                            new SimpleGrantedAuthority(
                                    role
                            )
                    )
            );

        } catch (UsernameNotFoundException e) {

            throw e;

        } catch (Exception e) {

            log.error(
                    "Load user failed",
                    e
            );

            throw new UsernameNotFoundException(
                    "Authentication failed"
            );
        }
    }

    // =====================================================
    // LOGIN STEP 1
    // =====================================================

    public void loginAndSendOtp(

            String email,

            String password
    ) {

        try {

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

            UserEntity user =
                    userRepository
                            .findByEmailIgnoreCase(
                                    normalizedEmail
                            )

                            .orElseThrow(() ->

                                    new ResponseStatusException(

                                            HttpStatus.NOT_FOUND,

                                            "User not found"
                                    )
                            );

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
            // PASSWORD CHECK
            // =====================================================

            if (

                    user.getPassword() == null ||

                            user.getPassword().isBlank()
            ) {

                throw new ResponseStatusException(

                        HttpStatus.UNAUTHORIZED,

                        "Invalid credentials"
                );
            }

            boolean matched;

            try {

                matched =
                        passwordEncoder.matches(

                                password,

                                user.getPassword()
                        );

            } catch (Exception e) {

                log.error(
                        "Password encoder failed",
                        e
                );

                throw new ResponseStatusException(

                        HttpStatus.UNAUTHORIZED,

                        "Invalid credentials"
                );
            }

            if (!matched) {

                throw new ResponseStatusException(

                        HttpStatus.UNAUTHORIZED,

                        "Invalid credentials"
                );
            }

            // =====================================================
            // GENERATE OTP
            // =====================================================

            String otp =
                    otpService.generateLoginOtp(user);

            log.info(

                    "Login OTP generated for {}",

                    normalizedEmail
            );

            // =====================================================
            // SEND OTP EMAIL
            // =====================================================

            emailService.sendVerificationOtpEmail(

                    normalizedEmail,

                    otp
            );

        } catch (ResponseStatusException e) {

            throw e;

        } catch (Exception e) {

            log.error(
                    "Login OTP flow failed",
                    e
            );

            throw new ResponseStatusException(

                    HttpStatus.INTERNAL_SERVER_ERROR,

                    "Login failed"
            );
        }
    }

    // =====================================================
    // LOGIN STEP 2
    // =====================================================

    @Transactional
    public AuthResponse verifyLoginOtp(

            UserEntity user,

            String otp
    ) {

        try {

            // =====================================================
            // VALIDATION
            // =====================================================

            if (user == null) {

                throw new ResponseStatusException(

                        HttpStatus.NOT_FOUND,

                        "User not found"
                );
            }

            if (

                    otp == null ||

                            otp.isBlank()
            ) {

                throw new ResponseStatusException(

                        HttpStatus.BAD_REQUEST,

                        "OTP required"
                );
            }

            // =====================================================
            // VERIFY OTP
            // =====================================================

            otpService.verifyLoginOtp(

                    user,

                    otp
            );

            // =====================================================
            // ROLE
            // =====================================================

            String role =

                    (user.getRole() != null &&

                            !user.getRole().isBlank())

                            ? user.getRole()

                            : "ROLE_USER";

            // =====================================================
            // FORCE PREFIX
            // =====================================================

            if (!role.startsWith("ROLE_")) {

                role = "ROLE_" + role;
            }

            // =====================================================
            // TOKEN VERSION
            // =====================================================

            Integer tokenVersion =
                    user.getTokenVersion();

            if (tokenVersion == null) {

                tokenVersion = 0;
            }

            // =====================================================
            // GENERATE JWT
            // =====================================================

            String token =
                    jwtUtil.generateAccessToken(

                            user.getEmail(),

                            tokenVersion,

                            role
                    );

            log.info(

                    "User {} logged in successfully with role {}",

                    user.getEmail(),

                    role
            );

            return AuthResponse.builder()

                    .token(token)

                    .userId(user.getUserId())

                    .build();

        } catch (ResponseStatusException e) {

            throw e;

        } catch (Exception e) {

            log.error(
                    "OTP verification failed",
                    e
            );

            throw new ResponseStatusException(

                    HttpStatus.INTERNAL_SERVER_ERROR,

                    "Login verification failed"
            );
        }
    }

    // =====================================================
    // PROFILE MAPPER
    // =====================================================

    public ProfileResponse mapToProfile(
            UserEntity user
    ) {

        try {

            if (user == null) {

                return null;
            }

            Optional<KycEntity> kycOptional =
                    kycRepository.findByUser(user);

            boolean isKycVerified = false;

            ProfileResponse.Kyc kycData = null;

            // =====================================================
            // KYC
            // =====================================================

            if (kycOptional.isPresent()) {

                KycEntity kyc =
                        kycOptional.get();

                String status =

                        kyc.getStatus() != null

                                ? kyc.getStatus().name()

                                : "PENDING";

                isKycVerified =

                        kyc.getStatus() != null

                                &&

                                "VERIFIED"
                                        .equalsIgnoreCase(
                                                kyc.getStatus().name()
                                        );

                kycData =
                        ProfileResponse.Kyc.builder()

                                .status(status)

                                .documentType(
                                        kyc.getDocumentType()
                                )

                                .documentNumber(
                                        kyc.getDocumentNumber()
                                )

                                .filePath(
                                        kyc.getFilePath()
                                )

                                .documentUrl(
                                        kyc.getFilePath()
                                )

                                .build();
            }

            // =====================================================
            // RESPONSE
            // =====================================================

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

                            .accountVerified(

                                    Boolean.TRUE.equals(
                                            user.getEmailVerified()
                                    )
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

                            .kycVerified(
                                    isKycVerified
                            )

                            .referralCode(
                                    user.getReferralCode()
                            )

                            .photoUrl(
                                    user.getPhotoUrl()
                            )

                            .profilePhotoUrl(
                                    user.getPhotoUrl()
                            )

                            .avatarUrl(
                                    user.getPhotoUrl()
                            )

                            .kyc(
                                    kycData
                            )

                            .build();

            if (response != null) {

                response.normalize();
            }

            return response;

        } catch (Exception e) {

            log.error(
                    "Profile mapping failed",
                    e
            );

            return null;
        }
    }
}