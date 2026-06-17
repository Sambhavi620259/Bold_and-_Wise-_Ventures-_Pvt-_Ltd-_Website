package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.AdminOtpVerificationEntity;
import in.bawvpl.Authify.repository.AdminOtpVerificationRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminOtpVerificationServiceImpl
        implements AdminOtpVerificationService {


    private final AdminOtpVerificationRepository
            repository;

    @Override
    public void markVerified(
            Long userId,
            String purpose,
            String actionToken
    ) {

        repository.deleteByUserIdAndPurpose(
                userId,
                purpose
        );

        AdminOtpVerificationEntity entity =
                AdminOtpVerificationEntity.builder()

                        .userId(userId)

                        .purpose(purpose)

                        .verified(true)

                        .verifiedAt(
                                LocalDateTime.now()
                        )

                        .expiresAt(
                                LocalDateTime.now()
                                        .plusMinutes(5)
                        )

                        .actionToken(
                                actionToken
                        )

                        .consumed(false)

                        .build();

        repository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isVerified(
            Long userId,
            String purpose
    ) {

        return repository
                .findFirstByUserIdAndPurposeOrderByVerifiedAtDesc(
                        userId,
                        purpose
                )
                .filter(AdminOtpVerificationEntity::getVerified)
                .filter(v ->
                        !Boolean.TRUE.equals(
                                v.getConsumed()
                        )
                )
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

        repository
                .findFirstByUserIdAndPurposeOrderByVerifiedAtDesc(
                        userId,
                        purpose
                )
                .ifPresent(v -> {

                    v.setConsumed(true);

                    v.setConsumedAt(
                            LocalDateTime.now()
                    );

                    repository.save(v);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public void validateActionToken(
            Long userId,
            String actionToken,
            String purpose
    ) {

        AdminOtpVerificationEntity entity =
                repository
                        .findByActionToken(
                                actionToken
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.FORBIDDEN,
                                        "Invalid action token"
                                )
                        );

        if (!entity.getUserId().equals(userId)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Token does not belong to user"
            );
        }

        if (!purpose.equals(entity.getPurpose())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Invalid token purpose"
            );
        }

        if (!Boolean.TRUE.equals(
                entity.getVerified()
        )) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "OTP not verified"
            );
        }

        if (Boolean.TRUE.equals(
                entity.getConsumed()
        )) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Token already consumed"
            );
        }

        if (
                entity.getExpiresAt()
                        .isBefore(
                                LocalDateTime.now()
                        )
        ) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Token expired"
            );
        }
    }

    @Override
    public void consumeActionToken(
            String actionToken
    ) {

        AdminOtpVerificationEntity entity =
                repository
                        .findByActionToken(
                                actionToken
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.FORBIDDEN,
                                        "Invalid action token"
                                )
                        );

        entity.setConsumed(true);

        entity.setConsumedAt(
                LocalDateTime.now()
        );

        repository.save(entity);
    }


}
