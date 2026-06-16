package in.bawvpl.Authify.controller;

import in.bawvpl.Authify.config.AdminManagementException;
import in.bawvpl.Authify.config.PrivilegeEscalationException;
import in.bawvpl.Authify.config.UnauthorizedRoleException;
import in.bawvpl.Authify.entity.UserEntity;
import in.bawvpl.Authify.entity.UserStatus;
import in.bawvpl.Authify.io.*;
import in.bawvpl.Authify.repository.UserRepository;
import in.bawvpl.Authify.service.AdminManagementService;
import in.bawvpl.Authify.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Admin Management Controller
 * CRITICAL: All endpoints are server-side authorized
 * Frontend authorization checks are NOT trusted
 */
@Slf4j
@RestController
@RequestMapping("/api/v1.0/admin")
@Tag(name = "Admin Management", description = "Admin account management endpoints")
public class AdminManagementController {

    private final AdminManagementService adminManagementService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public AdminManagementController(
            AdminManagementService adminManagementService,
            UserRepository userRepository,
            JwtUtil jwtUtil) {
        this.adminManagementService = adminManagementService;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    /**
     * GET /admin/admins - List all admins
     * Only SUPER_ADMIN and ADMIN can access
     */
    @GetMapping("/admins")
    @Operation(summary = "List all admin accounts", description = "Get paginated list of admin accounts. Only SUPER_ADMIN and ADMIN can access.")
    public ResponseEntity<?> listAdmins(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            UserEntity requestor = extractAndValidateUser(request);

            Pageable pageable = PageRequest.of(page, size);
            AdminListResponse response = adminManagementService.listAdmins(requestor, pageable);

            return ResponseEntity.ok(response);
        } catch (UnauthorizedRoleException | PrivilegeEscalationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse("Access Denied", e.getMessage()));
        } catch (Exception e) {
            log.error("Error listing admins", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error", "Failed to list admins"));
        }
    }

    /**
     * POST /admin/admins - Create new admin
     * SUPER_ADMIN only
     * CRITICAL: Cannot create SUPER_ADMIN via API
     */
    @PostMapping("/admins")
    @Operation(summary = "Create new admin account", description = "Create new admin account. SUPER_ADMIN only. Cannot create SUPER_ADMIN role.")
    public ResponseEntity<?> createAdmin(
            HttpServletRequest request,
            @Valid @RequestBody AdminCreateRequest adminRequest) {
        try {
            UserEntity requestor = extractAndValidateUser(request);
            AdminResponse response = adminManagementService.createAdmin(requestor, adminRequest);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (PrivilegeEscalationException e) {
            log.error("SECURITY: Privilege escalation attempt: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse("Privilege Escalation Blocked", e.getMessage()));
        } catch (AdminManagementException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Creation Failed", e.getMessage()));
        } catch (UnauthorizedRoleException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse("Access Denied", e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating admin", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error", "Failed to create admin"));
        }
    }

    /**
     * PUT /admin/admins/{id} - Update admin details
     * SUPER_ADMIN only
     */
    @PutMapping("/admins/{id}")
    @Operation(summary = "Update admin account", description = "Update admin account details. SUPER_ADMIN only.")
    public ResponseEntity<?> updateAdmin(
            HttpServletRequest request,
            @PathVariable Long id,
            @Valid @RequestBody AdminUpdateRequest updateRequest) {
        try {
            UserEntity requestor = extractAndValidateUser(request);
            AdminResponse response = adminManagementService.updateAdmin(requestor, id, updateRequest);

            return ResponseEntity.ok(response);
        } catch (UnauthorizedRoleException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse("Access Denied", e.getMessage()));
        } catch (AdminManagementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Not Found", e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating admin", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error", "Failed to update admin"));
        }
    }

    /**
     * PATCH /admin/admins/{id}/status - Enable/Disable admin
     * SUPER_ADMIN only
     * CRITICAL: Disabling admin invalidates their sessions
     */
    @PatchMapping("/admins/{id}/status")
    @Operation(summary = "Enable or disable admin account", description = "Enable or disable admin account. SUPER_ADMIN only. Disabling invalidates active sessions.")
    public ResponseEntity<?> updateAdminStatus(
            HttpServletRequest request,
            @PathVariable Long id,
            @Valid @RequestBody AdminStatusUpdateRequest statusRequest) {
        try {
            UserEntity requestor = extractAndValidateUser(request);
            AdminResponse response = adminManagementService.updateAdminStatus(requestor, id, statusRequest);

            return ResponseEntity.ok(response);
        } catch (UnauthorizedRoleException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse("Access Denied", e.getMessage()));
        } catch (AdminManagementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Not Found", e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating admin status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error", "Failed to update admin status"));
        }
    }

    /**
     * POST /admin/admins/{id}/reset-password - Reset admin password
     * SUPER_ADMIN only
     * CRITICAL: Resets token to force re-login
     */
    @PostMapping("/admins/{id}/reset-password")
    @Operation(summary = "Reset admin password", description = "Reset admin password. SUPER_ADMIN only. User must login again after reset.")
    public ResponseEntity<?> resetAdminPassword(
            HttpServletRequest request,
            @PathVariable Long id,
            @Valid @RequestBody AdminPasswordResetRequest resetRequest) {
        try {
            UserEntity requestor = extractAndValidateUser(request);
            AdminResponse response = adminManagementService.resetAdminPassword(requestor, id, resetRequest);

            return ResponseEntity.ok(new SuccessResponse("Password reset successfully", response));
        } catch (UnauthorizedRoleException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse("Access Denied", e.getMessage()));
        } catch (AdminManagementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Not Found", e.getMessage()));
        } catch (Exception e) {
            log.error("Error resetting admin password", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error", "Failed to reset password"));
        }
    }

    /**
     * Helper: Extract and validate user from JWT token
     * CRITICAL: All authorization happens here
     */
    private UserEntity extractAndValidateUser(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        if (token == null || token.isBlank()) {
            throw new UnauthorizedRoleException("No authentication token provided");
        }

        String email = jwtUtil.extractUsername(token);
        if (email == null || email.isBlank()) {
            throw new UnauthorizedRoleException("Invalid token");
        }

        UserEntity user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UnauthorizedRoleException("User not found"));

        if (user.getUserStatus() != UserStatus.ACTIVE) {

            throw new UnauthorizedRoleException(
                    "Admin account is disabled"
            );
        }

        return user;
    }

    /**
     * Helper: Extract JWT token from Authorization header
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    /**
     * Error response DTO
     */
    public static class ErrorResponse {
        public String error;
        public String message;

        public ErrorResponse(String error, String message) {
            this.error = error;
            this.message = message;
        }

        public String getError() { return error; }
        public String getMessage() { return message; }
    }

    /**
     * Success response DTO
     */
    public static class SuccessResponse {
        public String message;
        public Object data;

        public SuccessResponse(String message, Object data) {
            this.message = message;
            this.data = data;
        }

        public String getMessage() { return message; }
        public Object getData() { return data; }
    }
}

