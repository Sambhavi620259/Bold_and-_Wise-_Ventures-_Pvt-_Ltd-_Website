package in.bawvpl.Authify.repository;

import in.bawvpl.Authify.entity.AuditLog;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository
        extends JpaRepository<AuditLog, Long> {

    // =====================================================
    // FIND BY USER
    // =====================================================

    List<AuditLog> findByUserIdOrderByCreatedAtDesc(
            Long userId
    );

    // =====================================================
    // FIND BY ACTION
    // =====================================================

    List<AuditLog> findByActionOrderByCreatedAtDesc(
            String action
    );

    // =====================================================
    // LATEST AUDITS
    // =====================================================

    List<AuditLog> findTop100ByOrderByCreatedAtDesc();
}