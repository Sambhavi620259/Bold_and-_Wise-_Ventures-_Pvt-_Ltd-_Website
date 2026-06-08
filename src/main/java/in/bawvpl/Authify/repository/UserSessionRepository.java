package in.bawvpl.Authify.repository;

import in.bawvpl.Authify.entity.UserSession;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    // ================= GET ACTIVE SESSIONS =================
    List<UserSession> findByUserIdAndActiveTrue(Long userId);

    // ================= GET SPECIFIC SESSION =================
    Optional<UserSession> findByIdAndUserId(Long id, Long userId);

    // ================= FIND BY TOKEN =================
    Optional<UserSession> findByTokenAndActiveTrue(String token);

    // ================= GET ALL USER SESSIONS =================
    List<UserSession> findByUserId(Long userId);

    // ================= 🔥 DEACTIVATE ALL USER SESSIONS =================
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE UserSession s SET s.active = false WHERE s.userId = :userId")
    int deactivateAllByUserId(@Param("userId") Long userId);

    // ================= 🔥 LOGOUT SINGLE SESSION =================
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE UserSession s SET s.active = false WHERE s.token = :token")
    int deactivateByToken(@Param("token") String token);

    // ================= OPTIONAL CLEANUP =================
    void deleteByUserId(Long userId);
}