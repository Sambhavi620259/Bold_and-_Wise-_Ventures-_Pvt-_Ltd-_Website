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

    private static final String FOUNDER_REFERRAL_CODE = "BWVPL#26";

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

        } while (userRepository.findByReferralCode(code).isPresent());

        return code;
    }

    // =====================================================
    // APPLY REFERRAL
    // =====================================================

    public void applyReferral(
            UserEntity newUser,
            String referralCode
    ) {

        if (referralCode == null || referralCode.isBlank()) {
            referralCode = FOUNDER_REFERRAL_CODE;
        }

        referralCode = referralCode.trim().toUpperCase();

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

        // Store referral code
        newUser.setReferredBy(
                referrer.get().getReferralCode()
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