package com.examp.genifit.repository;

import com.examp.genifit.entity.PlanType;
import com.examp.genifit.entity.SubscriptionPlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscriptionPlanRepository
        extends JpaRepository<SubscriptionPlan, Integer> {

    Page<SubscriptionPlan> findByDeletedFalse(Pageable pageable);

    Page<SubscriptionPlan> findByActiveTrueAndDeletedFalse(Pageable pageable);

    Page<SubscriptionPlan> findByPlanNameContainingIgnoreCaseAndDeletedFalse(
            String keyword,
            Pageable pageable
    );

    Optional<SubscriptionPlan> findByPlanIdAndDeletedFalse(Integer planId);

    Optional<SubscriptionPlan> findByPlanTypeAndDeletedFalse(
            PlanType planType
    );

    boolean existsByPlanTypeAndDeletedFalse(
            PlanType planType
    );

    boolean existsByPlanNameIgnoreCaseAndDeletedFalse(
            String planName
    );

    Page<SubscriptionPlan> findByActiveTrueAndDeletedFalseOrderByPriceAsc(Pageable pageable);
}