package in.bawvpl.Authify.repository;

import in.bawvpl.Authify.entity.AdminAuditLogEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminAuditLogRepository
        extends JpaRepository<AdminAuditLogEntity, Long> {
}