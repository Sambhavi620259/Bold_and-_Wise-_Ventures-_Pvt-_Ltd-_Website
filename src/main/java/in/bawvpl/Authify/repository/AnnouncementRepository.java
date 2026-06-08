package in.bawvpl.Authify.repository;

import in.bawvpl.Authify.entity.AnnouncementEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnouncementRepository
        extends JpaRepository<AnnouncementEntity, Long> {

    // =====================================================
    // ACTIVE ANNOUNCEMENTS
    // =====================================================

    List<AnnouncementEntity>
    findByPublishedTrueOrderByCreatedAtDesc();

    // =====================================================
    // ADMIN PAGINATION
    // =====================================================

    Page<AnnouncementEntity>
    findAllByOrderByCreatedAtDesc(
            Pageable pageable
    );

    // =====================================================
    // ACTIVE PAGINATION
    // =====================================================

    Page<AnnouncementEntity>
    findByPublishedTrueOrderByCreatedAtDesc(
            Pageable pageable
    );

    // =====================================================
    // COUNT
    // =====================================================

    long countByPublishedTrue();
}
