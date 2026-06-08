package in.bawvpl.Authify.repository;

import in.bawvpl.Authify.entity.ApplicationEntity;
import in.bawvpl.Authify.entity.AppStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

@Repository
public interface AppRepository
        extends JpaRepository<ApplicationEntity, Long> {

    // =====================================================
    // USER APPS
    // =====================================================

    Page<ApplicationEntity> findByUser_Id(

            Long userId,

            Pageable pageable
    );

    long countByUser_Id(
            Long userId
    );

    // =====================================================
    // SLUG
    // =====================================================

    boolean existsBySlug(
            String slug
    );

    // =====================================================
    // STATUS
    // =====================================================

    Page<ApplicationEntity> findByStatus(

            AppStatus status,

            Pageable pageable
    );

    // =====================================================
    // SEARCH
    // =====================================================

    Page<ApplicationEntity>
    findByNameContainingIgnoreCase(

            String name,

            Pageable pageable
    );

    // =====================================================
    // FILTER + SEARCH
    // =====================================================

    Page<ApplicationEntity>
    findByStatusAndNameContainingIgnoreCase(

            AppStatus status,

            String name,

            Pageable pageable
    );

    // =====================================================
    // PUBLIC APPS
    // =====================================================

    Page<ApplicationEntity>
    findByVisibilityIgnoreCase(

            String visibility,

            Pageable pageable
    );

    // =====================================================
    // PUBLIC + STATUS
    // =====================================================

    Page<ApplicationEntity>
    findByStatusAndVisibilityIgnoreCase(

            AppStatus status,

            String visibility,

            Pageable pageable
    );

    // =====================================================
    // COUNT STATUS
    // =====================================================

    long countByStatus(
            AppStatus status
    );

    // =====================================================
    // COUNT PUBLIC
    // =====================================================

    long countByVisibilityIgnoreCase(
            String visibility
    );
}