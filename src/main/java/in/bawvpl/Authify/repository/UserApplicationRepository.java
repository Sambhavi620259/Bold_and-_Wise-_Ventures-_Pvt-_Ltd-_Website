package in.bawvpl.Authify.repository;

import in.bawvpl.Authify.entity.UserApplicationEntity;
import in.bawvpl.Authify.entity.UserEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserApplicationRepository
        extends JpaRepository<UserApplicationEntity, Long> {

    // =====================================================
    // FIND ONE
    // =====================================================

    Optional<UserApplicationEntity>
    findByUser_IdAndApp_AppId(
            Long userId,
            Long appId
    );

    // =====================================================
    // EXISTS
    // =====================================================

    boolean existsByUser_IdAndApp_AppId(
            Long userId,
            Long appId
    );

    // =====================================================
    // USER APPLICATIONS
    // =====================================================

    List<UserApplicationEntity>
    findAllByUser(
            UserEntity user
    );

    List<UserApplicationEntity>
    findAllByUser_Id(
            Long userId
    );

    // =====================================================
    // PAGINATION
    // =====================================================

    Page<UserApplicationEntity>
    findAllByUser_Id(
            Long userId,
            Pageable pageable
    );

    // =====================================================
    // RECENT APPS
    //
    // IMPORTANT:
    //
    // ONLY ACTUAL APP OPENS
    // SHOULD APPEAR HERE.
    //
    // NOT subscriptions.
    // =====================================================

    List<UserApplicationEntity>
    findTop10ByUser_IdAndLastOpenedAtIsNotNullOrderByLastOpenedAtDesc(
            Long userId
    );

    // =====================================================
    // COUNT
    // =====================================================

    long countByUser_Id(
            Long userId
    );

    // =====================================================
    // ACTIVE SUBSCRIPTIONS
    // =====================================================

    long countByUser_IdAndSubscriptionStatusIgnoreCase(
            Long userId,
            String subscriptionStatus
    );

    // =====================================================
    // ACTIVE USER APPS
    // =====================================================

    List<UserApplicationEntity>
    findByUser_IdAndActiveTrue(
            Long userId
    );

    // =====================================================
    // DISTINCT ACTIVE USER APPS
    // =====================================================

    @Query("""
            SELECT DISTINCT ua
            FROM UserApplicationEntity ua
            WHERE ua.user.id = :userId
            AND ua.active = true
            AND ua.app IS NOT NULL
            """)
    List<UserApplicationEntity>
    findDistinctActiveAppsByUserId(
            @Param("userId")
            Long userId
    );

    // =====================================================
    // FILTERED ACTIVE APPS
    // =====================================================

    @Query("""
            SELECT DISTINCT ua
            FROM UserApplicationEntity ua
            WHERE ua.user.id = :userId
            AND ua.app IS NOT NULL
            AND ua.active = true
            AND (
                    ua.app.visibility = 'PUBLIC'
                    OR ua.app.visibility IS NULL
                )
            ORDER BY ua.updatedAt DESC
            """)
    List<UserApplicationEntity>
    findVisibleAppsByUserId(
            @Param("userId")
            Long userId
    );

    // =====================================================
    // FILTERED PAGINATION
    // =====================================================

    @Query("""
            SELECT DISTINCT ua
            FROM UserApplicationEntity ua
            WHERE ua.user.id = :userId
            AND ua.app IS NOT NULL
            AND ua.active = true
            AND (
                    ua.app.visibility = 'PUBLIC'
                    OR ua.app.visibility IS NULL
                )
            """)
    Page<UserApplicationEntity>
    findVisibleAppsByUserId(
            @Param("userId")
            Long userId,
            Pageable pageable
    );

    // =====================================================
    // RECENTLY OPENED APPS
    //
    // IMPORTANT:
    //
    // Used by:
    // - dashboard recent apps
    // - app usage analytics
    //
    // MUST:
    // - exclude never-opened apps
    // - sort by lastOpenedAt
    // - exclude inactive apps
    //
    // MUST NOT:
    // - use subscription created date
    // - use updatedAt
    // =====================================================

    @Query("""
            SELECT DISTINCT ua
            FROM UserApplicationEntity ua
            WHERE ua.user.id = :userId
            AND ua.active = true
            AND ua.app IS NOT NULL
            AND ua.lastOpenedAt IS NOT NULL
            AND (
                    ua.app.visibility = 'PUBLIC'
                    OR ua.app.visibility IS NULL
                )
            ORDER BY ua.lastOpenedAt DESC
            """)
    List<UserApplicationEntity>
    findRecentlyOpenedAppsByUserId(
            @Param("userId")
            Long userId
    );

    // =====================================================
    // USAGE TIMESERIES
    //
    // IMPORTANT:
    //
    // Provides:
    // - date/hour bucket
    // - opens
    //
    // Used by:
    // - dashboard analytics graph
    // - usage chart
    //
    // IMPORTANT FIX:
    // Uses WITH RECURSIVE CTEs to guarantee that all timeline
    // buckets are populated with 0 values if no tracking exists.
    // =====================================================
    @Query(value = """
WITH RECURSIVE hours AS (

    SELECT DATE_FORMAT(
        DATE_SUB(
            DATE_FORMAT(
                CONVERT_TZ(
                    NOW(),
                    '+00:00',
                    '+05:30'
                ),
                '%Y-%m-%d %H:00:00'
            ),
            INTERVAL 23 HOUR
        ),
        '%Y-%m-%d %H:00:00'
    ) AS bucket

    UNION ALL

    SELECT DATE_FORMAT(
        DATE_ADD(bucket, INTERVAL 1 HOUR),
        '%Y-%m-%d %H:00:00'
    )
    FROM hours
    WHERE bucket <
          DATE_FORMAT(
              CONVERT_TZ(
                  NOW(),
                  '+00:00',
                  '+05:30'
              ),
              '%Y-%m-%d %H:00:00'
          )
)

SELECT
    h.bucket,
    COALESCE(COUNT(aul.id), 0) AS opens

FROM hours h

LEFT JOIN app_usage_log aul
    ON DATE_FORMAT(
           CONVERT_TZ(
               aul.opened_at,
               '+00:00',
               '+05:30'
           ),
           '%Y-%m-%d %H:00:00'
       ) = h.bucket

    AND aul.user_id = :userId

    AND (
        :appId IS NULL
        OR CAST(aul.app_id AS CHAR) = :appId
    )

GROUP BY h.bucket
ORDER BY h.bucket
""", nativeQuery = true)
    List<Object[]> getUsageTimeseries24H(
            @Param("userId") Long userId,
            @Param("appId") String appId
    );

    @Query(value = """
    WITH RECURSIVE days AS (
        SELECT CURDATE() - INTERVAL 6 DAY AS bucket
        UNION ALL
        SELECT bucket + INTERVAL 1 DAY
        FROM days
        WHERE bucket < CURDATE()
    )
    SELECT
        d.bucket,
        COALESCE(COUNT(aul.id), 0) AS opens
    FROM days d
    LEFT JOIN app_usage_log aul
        ON DATE(aul.opened_at) = d.bucket
        AND aul.user_id = :userId
        AND (
            :appId IS NULL
            OR CAST(aul.app_id AS CHAR) = :appId
        )
    GROUP BY d.bucket
    ORDER BY d.bucket
    """, nativeQuery = true)
    List<Object[]> getUsageTimeseries7D(
            @Param("userId") Long userId,
            @Param("appId") String appId
    );

    @Query(value = """
    WITH RECURSIVE days AS (
        SELECT CURDATE() - INTERVAL 29 DAY AS bucket
        UNION ALL
        SELECT bucket + INTERVAL 1 DAY
        FROM days
        WHERE bucket < CURDATE()
    )
    SELECT
        d.bucket,
        COALESCE(COUNT(aul.id), 0) AS opens
    FROM days d
    LEFT JOIN app_usage_log aul
        ON DATE(aul.opened_at) = d.bucket
        AND aul.user_id = :userId
        AND (
            :appId IS NULL
            OR CAST(aul.app_id AS CHAR) = :appId
        )
    GROUP BY d.bucket
    ORDER BY d.bucket
    """, nativeQuery = true)
    List<Object[]> getUsageTimeseries30D(
            @Param("userId") Long userId,
            @Param("appId") String appId
    );

}