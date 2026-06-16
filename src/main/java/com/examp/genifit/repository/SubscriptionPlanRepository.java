package com.examp.genifit.repository;

import com.examp.genifit.entity.PlanType;
import com.examp.genifit.entity.SubscriptionPlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Integer> {
    Optional<SubscriptionPlan> findByPlanType(PlanType planType);
    Page<SubscriptionPlan> findByActiveTrueOrderByPriceAsc(Pageable pageable);
    Page<SubscriptionPlan> findByActiveTrue(Pageable pageable);
    boolean existsByPlanType(PlanType planType);
}