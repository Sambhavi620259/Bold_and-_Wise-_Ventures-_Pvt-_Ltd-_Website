package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.UserEntity;
import in.bawvpl.Authify.entity.UserStatus;

import in.bawvpl.Authify.io.AdminUser;

import in.bawvpl.Authify.io.AdminUserResponse;
import in.bawvpl.Authify.repository.UserRepository;

import in.bawvpl.Authify.repository.UserSessionRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;

import org.springframework.stereotype.Service;

import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import in.bawvpl.Authify.entity.AdminRole;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
@Service
@RequiredArgsConstructor
public class AdminUserService {
    private final OtpService otpService;
    private final UserRepository userRepository;
    private final UserSessionService userSessionService;
    private final UserSessionRepository userSessionRepository;

    public Page<AdminUserResponse> getAdmins(
            int page,
            int size
    ) {

        Pageable pageable = PageRequest.of(page, size);

        Page<UserEntity> admins =
                userRepository.findByAdminRole(
                        AdminRole.ROLE_ADMIN,
                        pageable
                );

        List<AdminUserResponse> response =
                admins.getContent()
                        .stream()
                        .map(user ->
                                AdminUserResponse.builder()
                                        .id(user.getId())
                                        .userId(user.getUserId())
                                        .name(user.getEntityName())
                                        .fullName(user.getEntityName())
                                        .email(user.getEmail())
                                        .phoneNumber(user.getPhoneNumber())
                                        .role(user.getAdminRole().name())
                                        .status(
                                                user.getUserStatus() != null
                                                        ? user.getUserStatus().name()
                                                        : "ACTIVE"
                                        )
                                        .entityType(user.getEntityType())
                                        .createdAt(user.getCreatedAt())
                                        .build()
                        )
                        .toList();

        return new PageImpl<>(
                response,
                pageable,
                admins.getTotalElements()
        );
    }

    public Page<AdminUserResponse> getOwners(
            int page,
            int size
    ) {

        Pageable pageable = PageRequest.of(page, size);

        Page<UserEntity> owners =
                userRepository.findByAdminRole(
                        AdminRole.ROLE_OWNER,
                        pageable
                );

        List<AdminUserResponse> response =
                owners.getContent()
                        .stream()
                        .map(user ->
                                AdminUserResponse.builder()
                                        .id(user.getId())
                                        .userId(user.getUserId())
                                        .name(user.getEntityName())
                                        .fullName(user.getEntityName())
                                        .email(user.getEmail())
                                        .phoneNumber(user.getPhoneNumber())
                                        .role(user.getAdminRole().name())
                                        .status(
                                                user.getUserStatus() != null
                                                        ? user.getUserStatus().name()
                                                        : "ACTIVE"
                                        )
                                        .entityType(user.getEntityType())
                                        .createdAt(user.getCreatedAt())
                                        .build()
                        )
                        .toList();

        return new PageImpl<>(
                response,
                pageable,
                owners.getTotalElements()
        );
    }
    // =========================================
    // EMAIL CHANGE
    // =========================================
    public void requestEmailChange(
            String userId,
            String newEmail
    ) {

        UserEntity user =
                userRepository
                        .findByUserId(userId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "User not found"
                                )
                        );

        newEmail = newEmail.trim().toLowerCase();

        // Same email check
        if (newEmail.equalsIgnoreCase(user.getEmail())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "New email must be different from current email"
            );
        }

        // Existing email
        if (userRepository.existsByEmailIgnoreCase(newEmail)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email already exists"
            );
        }

        // Pending email
        if (userRepository.existsByPendingEmailIgnoreCase(newEmail)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email change already pending"
            );
        }

        // Save pending email
        user.setPendingEmail(newEmail);

        // Generate OTP
        otpService.generateEmailChangeOtp(user);

        // Save user
        userRepository.save(user);
    }
    public void verifyEmailChangeOtp(
            String userId,
            String otp
    ) {

        UserEntity user =
                userRepository
                        .findByUserId(userId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "User not found"
                                )
                        );

        // Check if an email change request exists
        if (user.getPendingEmail() == null ||
                user.getPendingEmail().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No email change request found"
            );
        }

        // Verify OTP
        otpService.verifyEmailChangeOtp(
                user,
                otp
        );

        // Update email after successful OTP verification
        user.setEmail(
                user.getPendingEmail()
        );

        // Clear pending email, OTP and expiry
        user.clearPendingEmailChange();

        // Invalidate authentication
        user.incrementTokenVersion();
        user.setRefreshToken(null);

        // Save updated user
        userRepository.save(user);

        // Logout all active sessions
        userSessionService.logoutAll(user.getId());

        // Deactivate stored sessions
        userSessionRepository.deactivateAllByUserId(user.getId());
    }
    public void resendEmailChangeOtp(
            String userId
    ) {
        UserEntity user =
                userRepository
                        .findByUserId(userId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "User not found"
                                )
                        );

        if (user.getPendingEmail() == null ||
                user.getPendingEmail().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No pending email change found"
            );
        }

        otpService.generateEmailChangeOtp(user);

        userRepository.save(user);
    }


    // =====================================================
    // TOTAL USERS
    // =====================================================

    public long totalUsers() {

        return userRepository
                .findAll()

                .stream()

                .filter(Objects::nonNull)

                .filter(user -> {

                    String role =
                            user.getRole() != null

                                    ? user.getRole()

                                    : "";

                    return !role.equalsIgnoreCase("ADMIN") &&
                            !role.equalsIgnoreCase("ROLE_ADMIN") &&
                            !role.equalsIgnoreCase("SUPER_ADMIN") &&
                            !role.equalsIgnoreCase("ROLE_SUPER_ADMIN");
                })

                .count();
    }

    // =====================================================
    // USER GROWTH
    // =====================================================

    public List<Map<String, Object>> getUserGrowth() {

        List<UserEntity> users =
                userRepository.findAll();

        Map<String, Long> grouped =
                new HashMap<>();

        for (UserEntity user : users) {

            if (user == null) {
                continue;
            }

            String role =
                    user.getRole() != null

                            ? user.getRole()

                            : "";

            if (
                    role.equalsIgnoreCase("ADMIN") ||
                            role.equalsIgnoreCase("ROLE_ADMIN") ||
                            role.equalsIgnoreCase("SUPER_ADMIN") ||
                            role.equalsIgnoreCase("ROLE_SUPER_ADMIN")
            ) {

                continue;
            }

            if (user.getCreatedAt() == null) {
                continue;
            }

            LocalDate date =
                    user.getCreatedAt()
                            .toLocalDate();

            String month =
                    date.getMonth()
                            .name()
                            .substring(0, 1) +

                            date.getMonth()
                                    .name()
                                    .substring(1, 3)
                                    .toLowerCase();

            grouped.put(

                    month,

                    grouped.getOrDefault(
                            month,
                            0L
                    ) + 1
            );
        }

        List<Map<String, Object>> growth =
                new ArrayList<>();

        grouped.forEach((month, count) -> {

            Map<String, Object> row =
                    new HashMap<>();

            // =====================================================
            // FRONTEND CONTRACT
            // =====================================================

            row.put("month", month);

            row.put("users", count);

            growth.add(row);
        });

        growth.sort(

                Comparator.comparing(
                        g -> g.get("month").toString()
                )
        );

        return growth;
    }

    // =====================================================
    // RECENT USERS
    // =====================================================

    public List<AdminUser> getRecentUsers(
            int limit
    ) {

        if (limit <= 0) {
            limit = 10;
        }

        if (limit > 100) {
            limit = 100;
        }

        List<UserEntity> users =
                userRepository.findAll();

        return users.stream()

                .filter(Objects::nonNull)

                .filter(user -> {

                    String role =
                            user.getRole() != null

                                    ? user.getRole()

                                    : "";
                    return !role.equalsIgnoreCase("ADMIN") &&

                            !role.equalsIgnoreCase("ROLE_ADMIN");
                })

                .sorted((a, b) -> {

                    if (

                            a.getCreatedAt() == null &&

                                    b.getCreatedAt() == null
                    ) {

                        return 0;
                    }

                    if (a.getCreatedAt() == null) {
                        return 1;
                    }

                    if (b.getCreatedAt() == null) {
                        return -1;
                    }

                    return b.getCreatedAt()
                            .compareTo(
                                    a.getCreatedAt()
                            );
                })

                .limit(limit)

                .map(user -> {

                    AdminUser adminUser =
                            new AdminUser();

                    adminUser.setId(
                            user.getId()
                    );

                    adminUser.setUserId(
                            user.getUserId()
                    );

                    adminUser.setName(
                            user.getEntityName()
                    );

                    adminUser.setFullName(
                            user.getEntityName()
                    );

                    adminUser.setEmail(
                            user.getEmail()
                    );

                    adminUser.setPhoneNumber(
                            user.getPhoneNumber()
                    );

                    adminUser.setRole(

                            user.getAdminRole() != null

                                    ? user.getAdminRole().name()

                                    : "ROLE_USER"
                    );

                    adminUser.setStatus(

                            user.getUserStatus() != null

                                    ? user.getUserStatus().name()

                                    : "ACTIVE"
                    );

                    adminUser.setKycStatus(

                            user.getKycStatus() != null

                                    ? user.getKycStatus()

                                    : "PENDING"
                    );

                    adminUser.setCreatedAt(

                            user.getCreatedAt() != null

                                    ? user.getCreatedAt()

                                    .atZone(
                                            ZoneId.systemDefault()
                                    )

                                    .toInstant()
                                    .toString()

                                    : Instant.now().toString()
                    );

                    try {

                        adminUser.normalize();

                    } catch (Exception ignored) {
                    }

                    return adminUser;
                })

                .collect(Collectors.toList());
    }

    // =====================================================
    // UPDATE USER
    // =====================================================

    public AdminUser updateUser(
            String userId,
            Map<String, Object> body
    ) {

        UserEntity user =

                userRepository
                        .findByUserId(userId)

                        .orElseThrow(() ->

                                new ResponseStatusException(

                                        HttpStatus.NOT_FOUND,

                                        "User not found"
                                )
                        );

        // =====================================================
        // NAME
        // =====================================================

        Object nameObj =
                body.get("name");

        if (
                nameObj != null &&
                        !nameObj.toString().isBlank()
        ) {

            user.setEntityName(
                    nameObj.toString().trim()
            );
        }

        // =====================================================
        // PHONE
        // =====================================================

        Object phoneObj =
                body.get("phoneNumber");

        if (
                phoneObj != null &&
                        !phoneObj.toString().isBlank()
        ) {

            user.setPhoneNumber(
                    phoneObj.toString().trim()
            );
        }

        // =====================================================
        // SAVE
        // =====================================================

        user =
                userRepository.save(user);

        AdminUser adminUser =
                new AdminUser();

        adminUser.setId(
                user.getId()
        );

        adminUser.setUserId(
                user.getUserId()
        );

        adminUser.setName(
                user.getEntityName()
        );

        adminUser.setFullName(
                user.getEntityName()
        );

        adminUser.setEmail(
                user.getEmail()
        );

        adminUser.setPhoneNumber(
                user.getPhoneNumber()
        );

        adminUser.setRole(

                user.getAdminRole() != null

                        ? user.getAdminRole().name()

                        : "ROLE_USER"
        );

        adminUser.setStatus(

                user.getUserStatus() != null

                        ? user.getUserStatus().name()

                        : "ACTIVE"
        );



                adminUser.setKycStatus(

                        user.getKycStatus() != null

                                ? user.getKycStatus()

                                : "PENDING"
                );

        adminUser.setCreatedAt(

                user.getCreatedAt() != null

                        ? user.getCreatedAt()

                        .atZone(
                                ZoneId.systemDefault()
                        )

                        .toInstant()
                        .toString()

                        : Instant.now().toString()
        );

        try {

            adminUser.normalize();

        } catch (Exception ignored) {
        }

        return adminUser;
    }

    // =====================================================
    // UPDATE USER STATUS
    // =====================================================

    public void updateUserStatus(

            String userId,

            boolean active
    ) {

        UserEntity user =

                userRepository
                        .findByUserId(userId)
                        .orElseThrow(() ->

                                new ResponseStatusException(

                                        HttpStatus.NOT_FOUND,

                                        "User not found"
                                )
                        );

        user.setUserStatus(

                active

                        ? UserStatus.ACTIVE

                        : UserStatus.BLOCKED
        );

        userRepository.save(user);
    }

}