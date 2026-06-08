package in.bawvpl.Authify.repository;

import in.bawvpl.Authify.entity.KycEntity;
import in.bawvpl.Authify.entity.KycStatus;
import in.bawvpl.Authify.entity.UserEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KycRepository
        extends JpaRepository<KycEntity, Long> {

    // =====================================================
    // USER
    // =====================================================

    @EntityGraph(attributePaths = {
            "user"
    })
    Optional<KycEntity> findByUser(
            UserEntity user
    );

    @EntityGraph(attributePaths = {
            "user"
    })
    Optional<KycEntity> findByUser_Id(
            Long userId
    );

    boolean existsByUser_Id(
            Long userId
    );

    // =====================================================
    // EMAIL LOOKUP
    // =====================================================

    @EntityGraph(attributePaths = {
            "user"
    })
    @Query("""

            SELECT k
            FROM KycEntity k
            WHERE LOWER(k.user.email) = LOWER(:email)

            """)
    Optional<KycEntity> findByUserEmail(
            @Param("email")
            String email
    );

    // =====================================================
    // STATUS
    // =====================================================

    @EntityGraph(attributePaths = {
            "user"
    })
    List<KycEntity> findByStatus(
            KycStatus status
    );

    long countByStatus(
            KycStatus status
    );

    // =====================================================
    // STATUS + PAGINATION
    // =====================================================

    @EntityGraph(attributePaths = {
            "user"
    })
    Page<KycEntity> findByStatus(

            KycStatus status,

            Pageable pageable
    );

    // =====================================================
    // MULTI STATUS
    // =====================================================

    @EntityGraph(attributePaths = {
            "user"
    })
    List<KycEntity> findByStatusIn(

            List<KycStatus> statuses
    );

    @EntityGraph(attributePaths = {
            "user"
    })
    List<KycEntity> findByStatusInOrderByUploadedAtDesc(

            List<KycStatus> statuses
    );

    // =====================================================
    // ADMIN SORT
    // =====================================================

    @EntityGraph(attributePaths = {
            "user"
    })
    List<KycEntity>
    findAllByOrderByUploadedAtDesc();

    @EntityGraph(attributePaths = {
            "user"
    })
    Page<KycEntity>
    findAllByOrderByUploadedAtDesc(
            Pageable pageable
    );

    // =====================================================
    // ADMIN STATUS FILTERS
    // =====================================================

    @EntityGraph(attributePaths = {
            "user"
    })
    List<KycEntity>
    findAllByStatusOrderByUploadedAtDesc(
            KycStatus status
    );

    @EntityGraph(attributePaths = {
            "user"
    })
    Page<KycEntity>
    findAllByStatusOrderByUploadedAtDesc(

            KycStatus status,

            Pageable pageable
    );

    // =====================================================
    // COMPLETED
    // =====================================================

    @EntityGraph(attributePaths = {
            "user"
    })
    List<KycEntity>
    findAllByCompleted(
            Boolean completed
    );

    @EntityGraph(attributePaths = {
            "user"
    })
    List<KycEntity>
    findByCompletedTrueOrderByUploadedAtDesc();

    long countByCompletedTrue();

    // =====================================================
    // REVIEWED BY
    // =====================================================

    @EntityGraph(attributePaths = {
            "user"
    })
    List<KycEntity>
    findByReviewedBy(
            String reviewedBy
    );

    // =====================================================
    // VERIFIED
    // =====================================================

    @EntityGraph(attributePaths = {
            "user"
    })
    List<KycEntity>
    findByStatusOrderByUploadedAtDesc(
            KycStatus status
    );

    // =====================================================
    // PENDING QUEUE
    // =====================================================

    @EntityGraph(attributePaths = {
            "user"
    })
    @Query("""

            SELECT k
            FROM KycEntity k

            WHERE k.status IN :statuses

            ORDER BY k.uploadedAt DESC

            """)
    List<KycEntity> findPendingQueue(

            @Param("statuses")
            List<KycStatus> statuses
    );

    // =====================================================
    // DASHBOARD COUNTS
    // =====================================================

    long countByStatusIn(
            List<KycStatus> statuses
    );

    // =====================================================
    // RECENT VERIFIED
    // =====================================================

    @EntityGraph(attributePaths = {
            "user"
    })
    List<KycEntity>
    findTop10ByStatusOrderByVerifiedAtDesc(
            KycStatus status
    );

    // =====================================================
    // RECENT REJECTED
    // =====================================================

    @EntityGraph(attributePaths = {
            "user"
    })
    List<KycEntity>
    findTop10ByStatusOrderByUpdatedAtDesc(
            KycStatus status
    );

    // =====================================================
    // SEARCH
    // =====================================================

    @EntityGraph(attributePaths = {
            "user"
    })
    @Query("""

            SELECT k
            FROM KycEntity k

            WHERE

                LOWER(k.documentNumber)
                    LIKE LOWER(CONCAT('%', :query, '%'))

                OR

                LOWER(k.user.email)
                    LIKE LOWER(CONCAT('%', :query, '%'))

                OR

                LOWER(k.user.entityName)
                    LIKE LOWER(CONCAT('%', :query, '%'))

            ORDER BY k.uploadedAt DESC

            """)
    List<KycEntity> search(
            @Param("query")
            String query
    );

    // =====================================================
    // ADMIN ANALYTICS
    // =====================================================

    @Query("""

            SELECT COUNT(k)

            FROM KycEntity k

            WHERE k.uploadedAt >= CURRENT_DATE

            """)
    long countTodayUploads();

    @Query("""

            SELECT COUNT(k)

            FROM KycEntity k

            WHERE k.status = 'PENDING'

            """)
    long countPending();

    @Query("""

            SELECT COUNT(k)

            FROM KycEntity k

            WHERE k.status = 'VERIFIED'

            """)
    long countVerified();

    @Query("""

            SELECT COUNT(k)

            FROM KycEntity k

            WHERE k.status = 'REJECTED'

            """)
    long countRejected();
}