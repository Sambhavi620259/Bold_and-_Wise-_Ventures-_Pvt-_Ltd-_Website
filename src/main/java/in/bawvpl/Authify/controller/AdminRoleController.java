package in.bawvpl.Authify.controller;

import in.bawvpl.Authify.entity.UserEntity;
import in.bawvpl.Authify.io.AdminOtpRequest;
import in.bawvpl.Authify.io.RoleUpdateRequest;
import in.bawvpl.Authify.repository.UserRepository;
import in.bawvpl.Authify.service.AdminRoleService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1.0/admin/role")
@RequiredArgsConstructor
public class AdminRoleController {

    private final AdminRoleService adminRoleService;

    private final UserRepository userRepository;

    private UserEntity getCurrentUser(
            Principal principal
    ) {

        return userRepository
                .findByEmailIgnoreCase(
                        principal.getName()
                )
                .orElseThrow();
    }

    @PostMapping("/request-otp")
    public ResponseEntity<?> requestOtp(
            Principal principal
    ) {

        adminRoleService.requestRoleChangeOtp(
                getCurrentUser(principal)
        );

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "OTP sent"
                )
        );
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(

            Principal principal,

            @RequestBody
            AdminOtpRequest request
    ) {

        adminRoleService.verifyRoleChangeOtp(
                getCurrentUser(principal),
                request.getOtp()
        );

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "OTP verified"
                )
        );
    }

    @PatchMapping("/users/{userId}")
    public ResponseEntity<?> updateRole(

            Principal principal,

            @PathVariable
            String userId,

            @RequestBody
            RoleUpdateRequest request
    ) {

        adminRoleService.changeRole(
                getCurrentUser(principal),
                userId,
                request.getRole()
        );

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Role updated"
                )
        );
    }
}