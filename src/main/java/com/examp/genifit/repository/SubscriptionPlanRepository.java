package com.examp.genifit.repository;

import com.examp.genifit.entity.PlanType;
import com.examp.genifit.entity.SubscriptionPlan;
import com.examp.genifit.entity.SubscriptionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Integer> {
    Page<SubscriptionPlan> findByActiveTrueAndDeletedFalse(Pageable pageable);
    Page<SubscriptionPlan> findByDeletedFalse(Pageable pageable);
    Page<SubscriptionPlan> findByActiveTrueAndDeletedFalseOrderByPriceAsc(Pageable pageable);
    Page<SubscriptionPlan> findByPlanNameContainingIgnoreCaseAndDeletedFalse(
            String keyword,
            Pageable pageable
    );
    Optional<SubscriptionPlan> findByPlanTypeAndDeletedFalse(PlanType planType);
    Optional<SubscriptionPlan> findByPlanIdAndDeletedFalse(Integer planId);
    boolean existsByPlanTypeAndDeletedFalse(PlanType planType);

    List<SubscriptionPlan> findByStatus(SubscriptionStatus status);

}