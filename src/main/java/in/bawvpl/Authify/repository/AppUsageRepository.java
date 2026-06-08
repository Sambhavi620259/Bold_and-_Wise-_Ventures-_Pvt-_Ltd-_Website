package in.bawvpl.Authify.repository;

import in.bawvpl.Authify.entity.AppUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface AppUsageRepository extends JpaRepository<AppUsage, Long> {

    // 🔥 Recent apps
    @Query("""
        SELECT a FROM AppUsage a
        WHERE a.userId = :userId
        ORDER BY a.usedAt DESC
    """)
    List<AppUsage> findRecentApps(Long userId);

    // 🔥 Timeseries
    @Query("""
        SELECT a FROM AppUsage a
        WHERE a.userId = :userId
        AND a.appId = :appId
        AND a.usedAt BETWEEN :start AND :end
        ORDER BY a.usedAt
    """)
    List<AppUsage> findUsageBetween(Long userId, String appId,
                                    LocalDateTime start, LocalDateTime end);
}