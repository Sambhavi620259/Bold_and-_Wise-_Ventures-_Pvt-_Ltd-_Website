package in.bawvpl.Authify.controller;

import in.bawvpl.Authify.entity.UserEntity;
import in.bawvpl.Authify.repository.UserRepository;
import in.bawvpl.Authify.service.ReferralService;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1.0/referrals")
public class ReferralController {

    private final UserRepository userRepository;

    private final ReferralService referralService;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;



    // =====================================================
    // MY REFERRAL INFO
    // =====================================================

    @GetMapping("/me")
    public ResponseEntity<?> getMyReferralInfo(
            Authentication authentication
    ) {

        String email =
                authentication.getName();

        UserEntity user =
                userRepository
                        .findByEmailIgnoreCase(email)
                        .orElseThrow();

        String referralLink =
                frontendUrl +
                        "/register?ref=" +
                        user.getReferralCode();

        Map<String, Object> response =
                new HashMap<>();

        response.put(
                "referralCode",
                user.getReferralCode()
        );

        response.put(
                "referralLink",
                referralLink
        );

        response.put(
                "referralCount",
                referralService.getReferralCount(
                        user.getUserId()
                )
        );

        return ResponseEntity.ok(response);
    }

    // =====================================================
    // MY REFERRALS
    // =====================================================

    @GetMapping("/list")
    public ResponseEntity<?> getMyReferrals(
            Authentication authentication
    ) {

        String email =
                authentication.getName();

        UserEntity user =
                userRepository
                        .findByEmailIgnoreCase(email)
                        .orElseThrow();

        Map<String, Object> response =
                new HashMap<>();

        response.put(
                "success",
                true
        );

        response.put(
                "status",
                200
        );

        response.put(
                "data",
                referralService.getReferralResponses(
                        user.getUserId()
                )
        );

        return ResponseEntity.ok(response);
    }

    // =====================================================
    // MY REFERRAL COUNT
    // =====================================================

    @GetMapping("/count")
    public ResponseEntity<?> getReferralCount(
            Authentication authentication
    ) {

        String email =
                authentication.getName();

        UserEntity user =
                userRepository
                        .findByEmailIgnoreCase(email)
                        .orElseThrow();

        Map<String, Object> response =
                new HashMap<>();

        response.put(
                "success",
                true
        );

        response.put(
                "status",
                200
        );

        response.put(
                "count",
                referralService.getReferralCount(
                        user.getUserId()
                )
        );

        return ResponseEntity.ok(response);
    }

}