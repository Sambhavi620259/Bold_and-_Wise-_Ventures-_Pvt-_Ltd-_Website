package in.bawvpl.Authify.repository;

import in.bawvpl.Authify.entity.AdminOtpVerificationEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminOtpVerificationRepository
        extends JpaRepository<AdminOtpVerificationEntity, Long> {

    Optional<AdminOtpVerificationEntity>
    findFirstByUserIdAndPurposeOrderByVerifiedAtDesc(
            Long userId,
            String purpose
    );

    Optional<AdminOtpVerificationEntity>
    findByActionToken(
            String actionToken
    );

    void deleteByUserIdAndPurpose(
            Long userId,
            String purpose
    );
}