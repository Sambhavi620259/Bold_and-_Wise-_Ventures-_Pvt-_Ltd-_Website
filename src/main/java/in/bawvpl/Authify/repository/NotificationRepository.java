package in.bawvpl.Authify.repository;

import in.bawvpl.Authify.entity.NotificationEntity;
import in.bawvpl.Authify.entity.UserEntity;

import jakarta.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository
        extends JpaRepository<NotificationEntity, Long> {

    // =====================================================
    // GET ALL USER NOTIFICATIONS
    // =====================================================

    List<NotificationEntity>
    findByUserOrderByCreatedAtDesc(
            UserEntity user
    );

    // =====================================================
    // PAGINATION
    // =====================================================

    Page<NotificationEntity>
    findByUser_IdOrderByCreatedAtDesc(

            Long userId,

            Pageable pageable
    );

    // =====================================================
    // ADMIN / USER NOTIFICATIONS
    // =====================================================

    Page<NotificationEntity>
    findByUser_EmailIgnoreCaseOrderByCreatedAtDesc(

            String email,

            Pageable pageable
    );

    // =====================================================
    // SINGLE NOTIFICATION
    // =====================================================

    Optional<NotificationEntity>
    findByIdAndUser_Id(

            Long notificationId,

            Long userId
    );

    Optional<NotificationEntity>
    findByIdAndUser_EmailIgnoreCase(

            Long notificationId,

            String email
    );

    // =====================================================
    // UNREAD COUNT
    // =====================================================

    long countByUserAndReadFalse(
            UserEntity user
    );

    long countByUser_IdAndReadFalse(
            Long userId
    );

    long countByUser_EmailIgnoreCaseAndReadFalse(
            String email
    );

    // =====================================================
    // READ COUNT
    // =====================================================

    long countByUser_IdAndReadTrue(
            Long userId
    );

    // =====================================================
    // DELETE ALL USER NOTIFICATIONS
    // =====================================================

    @Transactional
    void deleteByUser_Id(
            Long userId
    );

    // =====================================================
    // MARK ALL AS READ
    // =====================================================

    @Transactional
    @Modifying(
            clearAutomatically = true,
            flushAutomatically = true
    )
    @Query("""

            UPDATE NotificationEntity n

            SET n.read = true,
                n.updatedAt = CURRENT_TIMESTAMP

            WHERE n.user.id = :userId
            AND n.read = false

            """)
    int markAllAsRead(
            @Param("userId")
            Long userId
    );

    // =====================================================
    // MARK ALL AS READ BY EMAIL
    // =====================================================

    @Transactional
    @Modifying(
            clearAutomatically = true,
            flushAutomatically = true
    )
    @Query("""

            UPDATE NotificationEntity n

            SET n.read = true,
                n.updatedAt = CURRENT_TIMESTAMP

            WHERE LOWER(n.user.email) = LOWER(:email)
            AND n.read = false

            """)
    int markAllAsReadByEmail(
            @Param("email")
            String email
    );

    // =====================================================
    // MARK ALL AS UNREAD
    // =====================================================

    @Transactional
    @Modifying(
            clearAutomatically = true,
            flushAutomatically = true
    )
    @Query("""

            UPDATE NotificationEntity n

            SET n.read = false,
                n.updatedAt = CURRENT_TIMESTAMP

            WHERE n.user.id = :userId

            """)
    int markAllAsUnread(
            @Param("userId")
            Long userId
    );

    // =====================================================
    // DELETE READ NOTIFICATIONS
    // =====================================================

    @Transactional
    @Modifying(
            clearAutomatically = true,
            flushAutomatically = true
    )
    @Query("""

            DELETE FROM NotificationEntity n

            WHERE n.user.id = :userId
            AND n.read = true

            """)
    int deleteReadNotifications(
            @Param("userId")
            Long userId
    );
}