package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.UserEntity;
import in.bawvpl.Authify.io.ReferralUserResponse;
import in.bawvpl.Authify.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReferralService {

    private final UserRepository userRepository;

    // =====================================================
    // GENERATE UNIQUE REFERRAL CODE
    // =====================================================

    public String generateUniqueReferralCode() {

        String code;

        do {

            code =
                    "REF-" +
                            UUID.randomUUID()
                                    .toString()
                                    .replace("-", "")
                                    .substring(0, 8)
                                    .toUpperCase();

        } while (
                userRepository
                        .findByReferralCode(code)
                        .isPresent()
        );

        return code;
    }

    // =====================================================
    // APPLY REFERRAL
    // =====================================================

    public void applyReferral(
            UserEntity newUser,
            String referralCode
    ) {

        // Founder default referral
        if (referralCode == null || referralCode.isBlank()) {
            referralCode = "BWVPL#26";
        }

        referralCode = referralCode.trim();

        Optional<UserEntity> referrer =
                userRepository.findByReferralCode(referralCode);

        if (referrer.isEmpty()) {
            return;
        }

        // Prevent self-referral
        if (
                newUser.getEmail() != null &&
                        newUser.getEmail().equalsIgnoreCase(
                                referrer.get().getEmail()
                        )
        ) {
            return;
        }

        newUser.setReferredBy(
                referrer.get().getUserId()
        );
    }

    // =====================================================
    // REFERRAL COUNT
    // =====================================================

    public long getReferralCount(
            String userId
    ) {

        return userRepository.countByReferredBy(
                userId
        );
    }

    // =====================================================
    // REFERRAL RESPONSE LIST
    // =====================================================

    public List<ReferralUserResponse> getReferralResponses(
            String userId
    ) {

        return userRepository
                .findByReferredBy(
                        userId
                )
                .stream()
                .map(user ->
                        ReferralUserResponse.builder()
                                .userId(
                                        user.getUserId()
                                )
                                .fullName(
                                        user.getDisplayName()
                                )
                                .build()
                )
                .toList();
    }
}