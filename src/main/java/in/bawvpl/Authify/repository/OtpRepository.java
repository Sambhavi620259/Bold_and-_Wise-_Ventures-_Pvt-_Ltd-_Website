package in.bawvpl.Authify.repository;

import in.bawvpl.Authify.entity.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<OtpVerification, Long> {

    // ================= GET LATEST OTP =================
    Optional<OtpVerification> findTopByEmailAndPurposeOrderByCreatedAtDesc(
            String email,
            String purpose
    );

    // ================= OPTIONAL: DELETE OLD OTP =================
    void deleteByEmailAndPurpose(String email, String purpose);
}