package com.examp.genifit.repository;

import com.examp.genifit.entity.PlanType;
import com.examp.genifit.entity.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Integer> {
    Optional<SubscriptionPlan> findByPlanType(PlanType planType);
    List<SubscriptionPlan> findByActiveTrueOrderByPriceAsc();
    boolean existsByPlanType(PlanType planType);
}