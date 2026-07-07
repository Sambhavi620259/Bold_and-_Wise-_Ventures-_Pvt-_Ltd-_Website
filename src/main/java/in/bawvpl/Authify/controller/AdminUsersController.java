package in.bawvpl.Authify.controller;

import in.bawvpl.Authify.entity.*;

import in.bawvpl.Authify.io.*;

import in.bawvpl.Authify.repository.*;

import in.bawvpl.Authify.service.AdminRoleService;
import in.bawvpl.Authify.service.AdminUserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.*;

import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1.0/admin")
@RequiredArgsConstructor
//@CrossOrigin("*")
@PreAuthorize("hasAnyRole('ADMIN','OWNER')")

public class AdminUsersController {

    private static final Set<String> VALID_STATUSES = Set.of(
            "ACTIVE",
            "PENDING",
            "BLOCKED",
            "SUSPENDED",
            "DELETED"
    );

    private final UserRepository userRepository;

    private final UserProfileHistoryRepository userProfileHistoryRepository;

    private final KycRepository kycRepository;

    private final TicketRepository ticketRepository;

    private final UserSessionRepository userSessionRepository;
    private final AdminRoleService adminRoleService;
    private final AdminUserService adminUserService;
    // =====================================================
    // GET USERS
    // IMPORTANT:
    // ADMIN users excluded
    // =====================================================

    @GetMapping("/users")
    public ResponseEntity<Page<AdminUserResponse>> getUsers(

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "20")
            int size,

            @RequestParam(required = false)
            String search
    ) {

        if (page < 0) {
            page = 0;
        }

        if (size <= 0) {
            size = 20;
        }

        if (size > 100) {
            size = 100;
        }

        Pageable pageable =
                PageRequest.of(page, size);

        Page<UserEntity> users;

        if (

                search != null &&

                        !search.trim().isBlank()
        ) {

            users =
                    userRepository
                            .findByEntityNameContainingIgnoreCaseOrEmailContainingIgnoreCase(

                                    search.trim(),

                                    search.trim(),

                                    pageable
                            );

        } else {

            users =
                    userRepository.findAll(pageable);
        }

        // =====================================================
        // FILTER ADMIN USERS
        // =====================================================

        List<AdminUserResponse> filtered =

                users.getContent()

                        .stream()

                        .filter(user -> {

                            if (user == null) {
                                return false;
                            }

                            String role =
                                    user.getRole() != null

                                            ? user.getRole()
                                            : "";

                            return !role.equalsIgnoreCase("ADMIN") &&
                                    !role.equalsIgnoreCase("ROLE_ADMIN") &&
                                    !role.equalsIgnoreCase("OWNER") &&
                                    !role.equalsIgnoreCase("ROLE_OWNER") &&
                                    !role.equalsIgnoreCase("SUPER_ADMIN") &&
                                    !role.equalsIgnoreCase("ROLE_SUPER_ADMIN");
                        })

                        .map(this::mapUser)

                        .collect(Collectors.toList());

        Page<AdminUserResponse> response =
                new PageImpl<>(

                        filtered,

                        pageable,

                        filtered.size()
                );

        return ResponseEntity.ok(response);
    }

    // =====================================================
// UPDATE USER STATUS
// =====================================================

    @PatchMapping("/users/{userId}/status")
    public ResponseEntity<String> updateStatus(

            @PathVariable
            String userId,

            @Valid
            @RequestBody
            UpdateStatusRequest request
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

        String status = validateStatus(request.getStatus());

        UserStatus newStatus = UserStatus.valueOf(status);

        // =====================================================
        // OWNER PROTECTION
        // =====================================================

        if (user.getAdminRole() == AdminRole.ROLE_OWNER
                && (newStatus == UserStatus.BLOCKED
                || newStatus == UserStatus.SUSPENDED)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Cannot deactivate OWNER"
            );
        }

        // =====================================================
        // UPDATE STATUS
        // =====================================================

        user.setUserStatus(newStatus);

        // =====================================================
        // LOGOUT USER ONLY WHEN BLOCKED/SUSPENDED
        // =====================================================

        if (newStatus == UserStatus.BLOCKED
                || newStatus == UserStatus.SUSPENDED) {

            user.incrementTokenVersion();

            user.setRefreshToken(null);

            userSessionRepository.deactivateAllByUserId(user.getId());
        }

        userRepository.save(user);

        return ResponseEntity.ok(
                "User status updated successfully"
        );
    }
    // =====================================================
// USER HISTORY
// =====================================================

    @GetMapping("/users/{userId}/history")
    public ResponseEntity<List<AdminUserHistoryResponse>> getUserHistory(
            @PathVariable String userId
    ) {

        List<AdminUserHistoryResponse> history =

                userProfileHistoryRepository
                        .findByUserIdOrderByChangedAtDesc(userId)
                        .stream()
                        .map(item ->

                                AdminUserHistoryResponse.builder()

                                        .userId(item.getUserId())

                                        .oldEmail(item.getOldEmail())
                                        .newEmail(item.getNewEmail())

                                        .oldPhone(item.getOldPhone())
                                        .newPhone(item.getNewPhone())

                                        .changedBy(item.getChangedBy())
                                        .changedAt(item.getChangedAt())

                                        .build()
                        )
                        .collect(Collectors.toList());

        return ResponseEntity.ok(history);
    }

    // =====================================================
    // UPDATE USER
    // =====================================================

    @PutMapping("/users/{userId}")
    public ResponseEntity<AdminUserResponse> updateUser(

            @PathVariable
            String userId,

            @Valid
            @RequestBody
            UpdateUserRequest request
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
        // UPDATE NAME
        // =====================================================

        if (

                request.getName() != null &&

                        !request.getName().isBlank()
        ) {

            user.setEntityName(
                    request.getName().trim()
            );
        }

        // =====================================================
        // UPDATE EMAIL
        // =====================================================



        // =====================================================
        // UPDATE PHONE
        // =====================================================

        if (request.getPhoneNumber() != null &&
                !request.getPhoneNumber().isBlank()) {

            String phone =
                    request.getPhoneNumber().trim();

            if (userRepository.existsByPhoneNumber(phone)
                    && !phone.equals(user.getPhoneNumber())) {

                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Phone number already exists"
                );
            }

            user.setPhoneNumber(phone);
        }

        // =====================================================
        // UPDATE STATUS
        // =====================================================

        if (

                request.getStatus() != null &&

                        !request.getStatus().isBlank()
        ) {

            String status =
                    validateStatus(
                            request.getStatus()
                    );

            user.setUserStatus(
                    UserStatus.valueOf(status)
            );
        }

        userRepository.save(user);

        return ResponseEntity.ok(
                mapUser(user)
        );
    }

    // =====================================================
    // VALIDATE STATUS
    // =====================================================

    private String validateStatus(
            String status
    ) {

        if (

                status == null ||

                        status.isBlank()
        ) {

            throw new ResponseStatusException(

                    HttpStatus.BAD_REQUEST,

                    "Status is required"
            );
        }

        String normalizedStatus =
                status.trim()
                        .toUpperCase();

        if (

                !VALID_STATUSES.contains(
                        normalizedStatus
                )
        ) {

            throw new ResponseStatusException(

                    HttpStatus.BAD_REQUEST,

                    "Invalid status"
            );
        }

        return normalizedStatus;
    }

    private AdminUserResponse mapUser(
            UserEntity user
    ) {

        long openTicketsCount = 0;

        if (
                user != null &&
                        user.getId() != null
        ) {

            List<TicketEntity> openTickets =
                    ticketRepository
                            .findByUser_IdAndStatusOrderByCreatedAtDesc(

                                    user.getId(),

                                    TicketEntity.Status.OPEN
                            );

            openTicketsCount =
                    openTickets.size();
        }

        // =====================================================
        // FETCH KYC
        // =====================================================

        KycEntity kyc =
                kycRepository
                        .findByUser(user)
                        .orElse(null);

        // =====================================================
        // KYC STATUS
        // =====================================================

        String kycStatus =
                kyc != null &&
                        kyc.getStatus() != null

                        ? kyc.getStatus().name()

                        : "PENDING";



        // =====================================================
        // USER STATUS
        // =====================================================

        String status =
                user.getUserStatus() != null

                        ? user.getUserStatus().name()

                        : "ACTIVE";

        // =====================================================
        // ROLE
        // =====================================================

        String role =
                user.getAdminRole() != null
                        ? user.getAdminRole().name()
                        : "ROLE_USER";

        // =====================================================
        // DOCUMENT URLS
        // =====================================================

        String documentUrl =
                kyc != null
                        ? kyc.getFilePath()
                        : null;

        String frontDocumentUrl =
                kyc != null
                        ? kyc.getFrontDocumentUrl()
                        : null;

        String backDocumentUrl =
                kyc != null
                        ? kyc.getBackDocumentUrl()
                        : null;

        String aadhaarFrontUrl =
                kyc != null
                        ? kyc.getAadhaarFrontUrl()
                        : null;

        String aadhaarBackUrl =
                kyc != null
                        ? kyc.getAadhaarBackUrl()
                        : null;

        String panCardUrl =
                kyc != null
                        ? kyc.getPanCardUrl()
                        : null;

        String passportUrl =
                kyc != null
                        ? kyc.getPassportUrl()
                        : null;

        String drivingLicenseUrl =
                kyc != null
                        ? kyc.getDrivingLicenseUrl()
                        : null;

        String referredByUserId =
                user.getReferredBy();

        String selfieUrl =
                kyc != null
                        ? kyc.getSelfieUrl()
                        : null;

        String livePhotoUrl =
                kyc != null
                        ? kyc.getLivePhotoUrl()
                        : null;

        AdminUserResponse response =

                AdminUserResponse.builder()

                        // =====================================================
                        // IDS
                        // =====================================================

                        .id(
                                user.getId()
                        )

                        .userId(
                                user.getUserId()
                        )

                        .referredByUserId(referredByUserId)

                        .referredBy(referredByUserId)

                        // =====================================================
                        // USER
                        // =====================================================

                        .name(
                                user.getEntityName()
                        )

                        .fullName(
                                user.getEntityName()
                        )

                        .email(
                                user.getEmail()
                        )

                        .pendingEmail(user.getPendingEmail())

                        .phoneNumber(
                                user.getPhoneNumber()
                        )

                        .entityType(user.getEntityType())

                        // =====================================================
                        // STATUS
                        // =====================================================

                        .kycStatus(
                                kycStatus
                        )

                        .status(
                                status
                        )

                        .userStatus(
                                status
                        )

                        .role(
                                role
                        )

                        .isActive(
                                UserStatus.ACTIVE.name()
                                        .equalsIgnoreCase(status)
                        )

                        // =====================================================
                        // DOCUMENT URLS
                        // =====================================================

                        .documentUrl(
                                documentUrl
                        )

                        .documentFile(
                                documentUrl
                        )

                        .frontDocumentUrl(
                                frontDocumentUrl
                        )

                        .backDocumentUrl(
                                backDocumentUrl
                        )

                        .aadhaarFrontUrl(
                                aadhaarFrontUrl
                        )

                        .aadhaarBackUrl(
                                aadhaarBackUrl
                        )

                        .panCardUrl(
                                panCardUrl
                        )

                        .passportUrl(
                                passportUrl
                        )

                        .drivingLicenseUrl(
                                drivingLicenseUrl
                        )

                        .selfieUrl(
                                selfieUrl
                        )

                        .livePhotoUrl(
                                livePhotoUrl
                        )

                        // =====================================================
                        // META
                        // =====================================================

                        .createdAt(
                                user.getCreatedAt()
                        )

                        .openTicketsCount(
                                openTicketsCount
                        )

                        .build();

        response.normalize();

        return response;
    }

    // =====================================================
    // STATUS REQUEST
    // =====================================================

    @Data
    public static class UpdateStatusRequest {

        @NotBlank(
                message = "Status is required"
        )
        private String status;
    }

    // =====================================================
    // UPDATE USER REQUEST
    // =====================================================

    @Data
    public static class UpdateUserRequest {

        private String name;

        private String email;

        private String phoneNumber;

        private String status;
    }

    @PatchMapping("/users/{userId}/role")
    public ResponseEntity<?> updateRole(

            java.security.Principal principal,

            @PathVariable String userId,

            @RequestBody RoleUpdateRequest request
    ) {

        UserEntity currentUser =
                userRepository
                        .findByEmailIgnoreCase(principal.getName())
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Admin not found"
                                )
                        );

        adminRoleService.changeRole(
                currentUser,
                userId,
                request.getRole()
        );

        return ResponseEntity.ok(
                java.util.Map.of(
                        "message",
                        "Role updated"
                )
        );
    }

    @PostMapping("/users/request-email-change")
    public ResponseEntity<?> requestEmailChange(
            @Valid @RequestBody EmailChangeRequest request
    ) {

        adminUserService.requestEmailChange(
                request.getUserId(),
                request.getNewEmail()
        );

        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "message", "OTP sent successfully."
                )
        );
    }
    @PostMapping("/users/verify-email-change-otp")
    public ResponseEntity<?> verifyEmailChangeOtp(
            @Valid @RequestBody VerifyEmailChangeOtpRequest request
    ) {

        adminUserService.verifyEmailChangeOtp(
                request.getUserId(),
                request.getOtp()
        );

        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "message", "Email updated successfully."
                )
        );
    }
    @PostMapping("/users/resend-email-change-otp")
    public ResponseEntity<?> resendEmailChangeOtp(
            @Valid @RequestBody ResendEmailChangeOtpRequest request
    ) {

        adminUserService.resendEmailChangeOtp(
                request.getUserId()
        );

        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "message", "OTP resent successfully."
                )
        );
    }
}