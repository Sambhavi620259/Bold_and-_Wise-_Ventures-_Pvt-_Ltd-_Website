package in.bawvpl.Authify.controller;

import in.bawvpl.Authify.entity.UserEntity;
import in.bawvpl.Authify.io.AdminInviteRequest;
import in.bawvpl.Authify.io.AdminInviteCompleteRequest;
import in.bawvpl.Authify.io.AdminOtpRequest;
import in.bawvpl.Authify.service.AdminInviteService;
import in.bawvpl.Authify.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1.0/admin/invite")
@RequiredArgsConstructor
public class AdminInviteController {

    private final AdminInviteService adminInviteService;

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

        adminInviteService.requestInviteOtp(
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

        adminInviteService.verifyInviteOtp(
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

    @PostMapping
    public ResponseEntity<?> createInvite(

            Principal principal,

            @RequestBody
            AdminInviteRequest request
    ) {

        adminInviteService.createInvite(
                getCurrentUser(principal),
                request
        );

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Invite created"
                )
        );
    }

    @GetMapping("/{token}")
    public ResponseEntity<?> getInvite(
            @PathVariable String token
    ) {

        return ResponseEntity.ok(
                adminInviteService.getInviteInfo(token)
        );
    }

    @PostMapping("/complete")
    public ResponseEntity<?> completeInvite(
            @RequestBody
            AdminInviteCompleteRequest request
    ) {

        adminInviteService.completeInvite(
                request
        );

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Invite accepted"
                )
        );
    }
}