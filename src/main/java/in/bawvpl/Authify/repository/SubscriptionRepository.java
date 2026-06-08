package in.bawvpl.Authify.repository;

import in.bawvpl.Authify.entity.SubscriptionEntity;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository
        extends JpaRepository<SubscriptionEntity, Long> {

    // =====================================================
    // ACTIVE SUBSCRIPTIONS COUNT
    // =====================================================

    long countByUser_IdAndStatusIgnoreCase(

            Long userId,

            String status
    );

    // =====================================================
    // USER SUBSCRIPTIONS
    // =====================================================

    List<SubscriptionEntity>
    findByUser_Id(

            Long userId
    );

    // =====================================================
    // USER ACTIVE SUBSCRIPTIONS
    // =====================================================

    List<SubscriptionEntity>
    findByUser_IdAndStatusIgnoreCase(

            Long userId,

            String status
    );

    // =====================================================
    // PLAN SUBSCRIPTION
    // =====================================================

    Optional<SubscriptionEntity>
    findByUser_IdAndPlan_Id(

            Long userId,

            Long planId
    );

    // =====================================================
    // EXISTS PLAN SUBSCRIPTION
    // =====================================================

    boolean existsByUser_IdAndPlan_Id(

            Long userId,

            Long planId
    );

    // =====================================================
    // EXISTS ACTIVE PLAN
    // =====================================================

    boolean existsByUser_IdAndPlan_IdAndStatusIgnoreCase(

            Long userId,

            Long planId,

            String status
    );
}