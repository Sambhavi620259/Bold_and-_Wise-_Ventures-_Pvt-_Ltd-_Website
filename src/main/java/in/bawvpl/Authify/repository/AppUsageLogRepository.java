package in.bawvpl.Authify.repository;

import in.bawvpl.Authify.entity.AppUsageLogEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppUsageLogRepository
        extends JpaRepository<AppUsageLogEntity, Long> {
}