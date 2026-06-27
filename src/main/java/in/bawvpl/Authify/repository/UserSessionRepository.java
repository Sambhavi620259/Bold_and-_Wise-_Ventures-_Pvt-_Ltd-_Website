package in.bawvpl.Authify.repository;

import in.bawvpl.Authify.entity.UserSession;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    // =====================================================
    // ACTIVE SESSIONS
    // =====================================================

    List<UserSession> findByUserIdAndActiveTrue(Long userId);

    // =====================================================
    // ALL USER SESSIONS
    // =====================================================

    List<UserSession> findByUserId(Long userId);

    // =====================================================
    // FIND SESSION BY ID
    // =====================================================

    Optional<UserSession> findByIdAndUserId(Long id, Long userId);

    // =====================================================
    // FIND ACTIVE SESSION BY TOKEN
    // =====================================================

    Optional<UserSession> findByTokenAndActiveTrue(String token);

    // =====================================================
    // CHECK ACTIVE TOKEN
    // =====================================================

    boolean existsByTokenAndActiveTrue(String token);

    // =====================================================
    // LOGOUT ALL DEVICES
    // =====================================================

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE UserSession s
            SET s.active = false
            WHERE s.userId = :userId
            """)
    int deactivateAllByUserId(@Param("userId") Long userId);

    // =====================================================
    // LOGOUT CURRENT DEVICE
    // =====================================================

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE UserSession s
            SET s.active = false
            WHERE s.token = :token
            """)
    int deactivateByToken(@Param("token") String token);

    // =====================================================
    // DELETE ALL USER SESSIONS
    // =====================================================

    @Transactional
    void deleteByUserId(Long userId);
}