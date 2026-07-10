package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.UserEntity;
import in.bawvpl.Authify.io.ReferralUserResponse;
import in.bawvpl.Authify.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReferralService {

    private static final String FOUNDER_REFERRAL_CODE = "BWVPL#26";

    private final UserRepository userRepository;

    // =====================================================
    // GENERATE UNIQUE REFERRAL CODE
    // =====================================================

    public String generateUniqueReferralCode(String entityType) {

        String prefix =
                "ORGANIZATION".equalsIgnoreCase(entityType)
                        ? "ORG-"
                        : "USR-";

        String code;

        do {

            code =
                    prefix +
                            UUID.randomUUID()
                                    .toString()
                                    .replace("-", "")
                                    .substring(0, 8)
                                    .toUpperCase();

        } while (userRepository.existsByReferralCode(code));

        return code;
    }

    public String resolveReferralValue(String value) {

        if (value == null || value.isBlank()) {
            return FOUNDER_REFERRAL_CODE;
        }

        value = value.trim();

        if (value.equalsIgnoreCase(FOUNDER_REFERRAL_CODE)) {
            return FOUNDER_REFERRAL_CODE;
        }

        if (value.toUpperCase().startsWith("USR-")) {
            return value.toUpperCase();
        }

        if (value.toUpperCase().startsWith("ORG-")) {
            return value.toUpperCase();
        }

        Optional<UserEntity> user =
                userRepository.findByUserId(value);

        if (user.isPresent()) {
            return user.get().getReferralCode();
        }

// Unknown value - let applyReferral() validate it
        return value.toUpperCase();
    }

    // =====================================================
    // APPLY REFERRAL
    // =====================================================

    public void applyReferral(
            UserEntity newUser,
            String referralCode
    ) {

        // No referral supplied
        if (referralCode == null || referralCode.isBlank()) {

            newUser.setReferredBy(
                    FOUNDER_REFERRAL_CODE
            );

            return;
        }

        referralCode = referralCode.trim().toUpperCase();

        Optional<UserEntity> referrer =
                userRepository.findByReferralCode(
                        referralCode
                );

        if (referrer.isEmpty()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid referral code"
            );
        }

        UserEntity referrerUser = referrer.get();

        // Prevent self referral by email
        if (
                newUser.getEmail() != null &&
                        newUser.getEmail().equalsIgnoreCase(
                                referrerUser.getEmail()
                        )
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Self referral is not allowed"
            );
        }

        // Prevent self referral by phone
        if (
                newUser.getPhoneNumber() != null &&
                        referrerUser.getPhoneNumber() != null &&
                        newUser.getPhoneNumber().equals(
                                referrerUser.getPhoneNumber()
                        )
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Self referral is not allowed"
            );
        }

        newUser.setReferredBy(
                referrerUser.getReferralCode()
        );
    }

    // =====================================================
    // REFERRAL COUNT
    // =====================================================

    public long getReferralCount(
            String referralCode
    ) {

        return userRepository.countByReferredBy(
                referralCode
        );
    }

    // =====================================================
    // REFERRAL RESPONSE LIST
    // =====================================================

    public List<ReferralUserResponse> getReferralResponses(
            String referralCode
    ) {

        return userRepository
                .findByReferredBy(referralCode)
                .stream()
                .map(user ->
                        ReferralUserResponse.builder()
                                .userId(user.getUserId())
                                .fullName(user.getDisplayName())
                                .build()
                )
                .toList();
    }
}