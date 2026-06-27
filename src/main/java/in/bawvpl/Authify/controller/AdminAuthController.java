package in.bawvpl.Authify.controller;

import in.bawvpl.Authify.entity.UserEntity;
import in.bawvpl.Authify.entity.UserSession;

import in.bawvpl.Authify.io.AdminRegisterRequest;
import in.bawvpl.Authify.io.ApiResponse;

import in.bawvpl.Authify.repository.UserRepository;
import in.bawvpl.Authify.repository.UserSessionRepository;

import in.bawvpl.Authify.service.AdminAuthService;
import in.bawvpl.Authify.service.OtpService;

import in.bawvpl.Authify.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.bind.annotation.*;

import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1.0/admin/auth")
@RequiredArgsConstructor
@Slf4j
//@CrossOrigin("*")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    private final UserRepository userRepository;

    private final UserSessionRepository userSessionRepository;

    private final OtpService otpService;

    private final JwtUtil jwtUtil;

    // =====================================================
    // ADMIN REGISTER
    // =====================================================

    @PostMapping("/register")
    public ApiResponse<?> registerAdmin(
            @RequestBody AdminRegisterRequest request
    ) {

        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Admin registration disabled. Use invite system."
        );
    }

    // =====================================================
    // ADMIN LOGIN
    // =====================================================

    @PostMapping("/login")
    public ResponseEntity<?> login(

            @RequestBody
            AdminLoginRequest request
    ) {

        if (

                request.getEmail() == null ||

                        request.getEmail().isBlank()

        ) {

            throw new ResponseStatusException(

                    HttpStatus.BAD_REQUEST,

                    "Email required"
            );
        }

        if (

                request.getPassword() == null ||

                        request.getPassword().isBlank()

        ) {

            throw new ResponseStatusException(

                    HttpStatus.BAD_REQUEST,

                    "Password required"
            );
        }

        adminAuthService.loginAndSendOtp(

                request.getEmail(),

                request.getPassword()
        );

        return ResponseEntity.ok(

                Map.of(

                        "success",
                        true,

                        "message",
                        "OTP sent successfully",

                        "nextStep",
                        "VERIFY_OTP"
                )
        );
    }

    // =====================================================
    // VERIFY OTP
    // =====================================================

    @Transactional
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(

            @RequestBody
            AdminVerifyOtpRequest request,

            HttpServletRequest httpRequest
    ) {

        if (

                request.getEmail() == null ||

                        request.getEmail().isBlank()

                        ||

                        request.getOtp() == null ||

                        request.getOtp().isBlank()

        ) {

            throw new ResponseStatusException(

                    HttpStatus.BAD_REQUEST,

                    "Email and OTP required"
            );
        }

        String email =
                request.getEmail()
                        .trim()
                        .toLowerCase();

        // =====================================================
        // FIND USER
        // =====================================================

        UserEntity user =
                userRepository
                        .findByEmailIgnoreCase(email)
                        .orElseThrow(() ->

                                new ResponseStatusException(

                                        HttpStatus.NOT_FOUND,

                                        "Admin not found"
                                )
                        );

        // =====================================================
        // ROLE CHECK
        // IMPORTANT FIX
        // =====================================================

        String role =
                user.getAdminRole().name();

        if (
                role == null || role.isBlank()

        ) {

            role = "ROLE_USER";
        }

        if (!role.startsWith("ROLE_")) {

            role = "ROLE_" + role;
        }

        role = role.toUpperCase();

        if (

                !"ROLE_ADMIN".equalsIgnoreCase(role)

                        &&

                        !"ROLE_OWNER".equalsIgnoreCase(role)

        ) {

            throw new ResponseStatusException(

                    HttpStatus.FORBIDDEN,

                    "Admin access denied"
            );
        }

        // =====================================================
        // VERIFY OTP
        // =====================================================

        otpService.verifyLoginOtp(

                user,

                request.getOtp()
        );

        // =====================================================
        // GENERATE JWT
        // =====================================================

        String token =
                jwtUtil.generateAccessToken(

                        user.getEmail(),

                        user.getTokenVersion(),

                        role
                );

        log.info(
                "ADMIN JWT GENERATED : {}",
                role
        );

        // =====================================================
        // REMOVE OLD SESSIONS
        // =====================================================

        userSessionRepository
                .deactivateAllByUserId(
                        user.getId()
                );

        // =====================================================
        // CREATE SESSION
        // =====================================================

        UserSession session =
                new UserSession();

        session.setUserId(
                user.getId()
        );

        session.setToken(token);

        session.setActive(true);

        session.setIpAddress(
                httpRequest.getRemoteAddr()
        );

        session.setDeviceName(
                httpRequest.getHeader("User-Agent")
        );

        session.setLoginTime(
                LocalDateTime.now()
        );

        session.setLastAccessTime(
                LocalDateTime.now()
        );

        userSessionRepository.save(session);

        // =====================================================
        // RESPONSE
        // =====================================================

        return ResponseEntity.ok(

                Map.of(

                        "success",
                        true,

                        "message",
                        "Admin login successful",

                        "role",
                        role,

                        "token",
                        token
                )
        );
    }

    // =====================================================
    // LOGIN DTO
    // =====================================================

    @Data
    public static class AdminLoginRequest {

        private String email;

        private String password;
    }

    // =====================================================
    // OTP DTO
    // =====================================================

    @Data
    public static class AdminVerifyOtpRequest {

        private String email;

        private String otp;
    }
}