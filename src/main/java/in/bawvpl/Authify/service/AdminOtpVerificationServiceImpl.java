package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.AdminOtpVerificationEntity;
import in.bawvpl.Authify.repository.AdminOtpVerificationRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminOtpVerificationServiceImpl
        implements AdminOtpVerificationService {

    private final AdminOtpVerificationRepository
            adminOtpVerificationRepository;

    @Override
    public void markVerified(
            Long userId,
            String purpose
    ) {

        adminOtpVerificationRepository
                .deleteByUserIdAndPurpose(
                        userId,
                        purpose
                );

        AdminOtpVerificationEntity verification =
                AdminOtpVerificationEntity.builder()

                        .userId(
                                userId
                        )

                        .purpose(
                                purpose
                        )

                        .verified(
                                true
                        )

                        .verifiedAt(
                                LocalDateTime.now()
                        )

                        .expiresAt(
                                LocalDateTime.now()
                                        .plusMinutes(15)
                        )

                        .build();

        adminOtpVerificationRepository.save(
                verification
        );
    }

    @Override
    public boolean isVerified(
            Long userId,
            String purpose
    ) {

        return adminOtpVerificationRepository

                .findFirstByUserIdAndPurposeOrderByVerifiedAtDesc(
                        userId,
                        purpose
                )

                .filter(AdminOtpVerificationEntity::getVerified)

                .filter(v ->
                        v.getExpiresAt()
                                .isAfter(
                                        LocalDateTime.now()
                                )
                )

                .isPresent();
    }

    @Override
    public void consumeVerification(
            Long userId,
            String purpose
    ) {

        adminOtpVerificationRepository
                .deleteByUserIdAndPurpose(
                        userId,
                        purpose
                );
    }
}