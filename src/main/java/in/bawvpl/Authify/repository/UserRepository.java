package in.bawvpl.Authify.repository;

import in.bawvpl.Authify.entity.UserEntity;
import in.bawvpl.Authify.entity.UserStatus;
import in.bawvpl.Authify.entity.AdminRole;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository
        extends JpaRepository<UserEntity, Long> {

    // =====================================================
    // AUTH
    // =====================================================

    Optional<UserEntity> findByEmail(
            String email
    );

    Optional<UserEntity> findByEmailIgnoreCase(
            String email
    );

    boolean existsByEmail(
            String email
    );

    boolean existsByEmailIgnoreCase(
            String email
    );

    // =====================================================
    // PHONE
    // =====================================================

    boolean existsByPhoneNumber(
            String phoneNumber
    );

    Optional<UserEntity> findByPhoneNumber(
            String phoneNumber
    );

    // =====================================================
    // USER ID
    // =====================================================

    Optional<UserEntity> findByUserId(
            String userId
    );

    // =====================================================
    // ENTITY ID
    // =====================================================

    Optional<UserEntity> findByEntityId(
            Long entityId
    );

    boolean existsByEntityId(
            Long entityId
    );

    Optional<UserEntity> findTopByOrderByEntityIdDesc();

    // =====================================================
    // EMAIL VERIFICATION
    // =====================================================

    Optional<UserEntity> findByVerificationToken(
            String verificationToken
    );

    // =====================================================
    // EMAIL CHANGE
    // =====================================================

    boolean existsByPendingEmailIgnoreCase(
            String pendingEmail
    );
    // =====================================================
    // REFERRAL
    // =====================================================

    Optional<UserEntity> findByReferralCode(
            String referralCode
    );

    boolean existsByReferralCode(
            String referralCode
    );

    List<UserEntity> findByReferredBy(
            String referredBy
    );

    long countByReferredBy(
            String referredBy
    );

    // =====================================================
    // ADMIN SEARCH
    // =====================================================

    Page<UserEntity>
    findByEntityNameContainingIgnoreCaseOrEmailContainingIgnoreCase(

            String entityName,

            String email,

            Pageable pageable
    );

    // =====================================================
    // STATUS FILTER
    // =====================================================

    Page<UserEntity>
    findByUserStatus(

            UserStatus userStatus,

            Pageable pageable
    );

    // =====================================================
    // ACTIVE USERS COUNT
    // =====================================================

    long countByUserStatus(
            UserStatus userStatus
    );

    // =====================================================
    // ACTIVE NORMAL USERS ONLY (TYPED ENUM QUERY)
    //
    // Uses:
    // - adminRole (Enum type)
    //
    // MUST exclude:
    // - ROLE_ADMIN
    // - ROLE_SUPER_ADMIN
    // =====================================================

    long countByUserStatusAndAdminRole(
            UserStatus userStatus,
            AdminRole adminRole
    );

    Page<UserEntity> findByAdminRole(

            AdminRole adminRole,

            Pageable pageable
    );

    // =====================================================
    // OWNER COUNT
    // =====================================================

    long countByAdminRole(
            AdminRole adminRole
    );

    // =====================================================
    // ORGANIZATION COUNT
    // =====================================================

    long countByEntityTypeIgnoreCase(
            String entityType
    );
}