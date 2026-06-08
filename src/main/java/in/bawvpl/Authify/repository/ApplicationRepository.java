package in.bawvpl.Authify.repository;

import in.bawvpl.Authify.entity.ApplicationEntity;
import in.bawvpl.Authify.entity.AppStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository
        extends JpaRepository<ApplicationEntity, Long> {

    // =====================================================
    // USER APPS
    // =====================================================

    Page<ApplicationEntity>
    findByUser_Id(

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

    Optional<ApplicationEntity>
    findBySlug(
            String slug
    );

    // =====================================================
    // STATUS
    // =====================================================

    Page<ApplicationEntity>
    findByStatus(

            AppStatus status,

            Pageable pageable
    );

    List<ApplicationEntity>
    findByStatus(
            AppStatus status
    );

    // =====================================================
    // STATUS + VISIBILITY
    // =====================================================

    Page<ApplicationEntity>
    findByStatusAndVisibility(

            AppStatus status,

            String visibility,

            Pageable pageable
    );

    List<ApplicationEntity>
    findByStatusAndVisibility(

            AppStatus status,

            String visibility
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
    // STATUS + VISIBILITY + SEARCH
    // =====================================================

    Page<ApplicationEntity>
    findByStatusAndVisibilityAndNameContainingIgnoreCase(

            AppStatus status,

            String visibility,

            String name,

            Pageable pageable
    );

    // =====================================================
    // FEATURED APPS
    // =====================================================

    List<ApplicationEntity>
    findByFeaturedTrue();

    List<ApplicationEntity>
    findByFeaturedTrueAndStatus(

            AppStatus status
    );

    // =====================================================
    // PUBLIC APPS
    // =====================================================

    List<ApplicationEntity>
    findByVisibilityIgnoreCase(
            String visibility
    );

    // =====================================================
    // ACTIVE PUBLIC APPS
    // =====================================================

    List<ApplicationEntity>
    findByStatusAndVisibilityIgnoreCase(

            AppStatus status,

            String visibility
    );

    // =====================================================
    // PUBLISHED PUBLIC APPS
    // =====================================================

    List<ApplicationEntity>
    findByStatusAndVisibilityIgnoreCaseOrderByCreatedAtDesc(

            AppStatus status,

            String visibility
    );

    // =====================================================
    // FEATURED PUBLISHED APPS
    // =====================================================

    List<ApplicationEntity>
    findByFeaturedTrueAndStatusAndVisibilityIgnoreCase(

            AppStatus status,

            String visibility
    );

    // =====================================================
    // GLOBAL PUBLISHED PUBLIC APP COUNT
    //
    // IMPORTANT:
    //
    // Used by dashboard summary.
    //
    // MUST count:
    // - PUBLIC apps
    // - PUBLISHED apps
    //
    // MUST NOT count:
    // - user subscriptions
    // =====================================================

    long countByStatusAndVisibilityIgnoreCase(

            AppStatus status,

            String visibility
    );

    // =====================================================
    // ADMIN VISIBILITY FILTER
    // =====================================================

    Page<ApplicationEntity>
    findByVisibilityIgnoreCase(

            String visibility,

            Pageable pageable
    );

    // =====================================================
    // ADMIN VISIBILITY + SEARCH
    // =====================================================

    Page<ApplicationEntity>
    findByVisibilityIgnoreCaseAndNameContainingIgnoreCase(

            String visibility,

            String name,

            Pageable pageable
    );

    // =====================================================
    // DASHBOARD GLOBAL APP COUNT
    // =====================================================

    default long countPublishedPublicApps() {

        return countByStatusAndVisibilityIgnoreCase(

                AppStatus.PUBLISHED,

                "PUBLIC"
        );
    }
}