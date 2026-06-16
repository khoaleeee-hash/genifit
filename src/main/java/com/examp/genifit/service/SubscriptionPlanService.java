package com.examp.genifit.service;

import com.examp.genifit.dto.request.CreateSubscriptionPlanRequest;
import com.examp.genifit.dto.request.UpdateSubscriptionPlanRequest;
import com.examp.genifit.dto.response.SubscriptionPlanResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface SubscriptionPlanService {
    Page<SubscriptionPlanResponse> getActivePlans(Integer pageNum, Integer pageSize);
    SubscriptionPlanResponse getPlanById(Integer planId);
    SubscriptionPlanResponse createPlan(CreateSubscriptionPlanRequest request);
    SubscriptionPlanResponse updatePlan(Integer planId, UpdateSubscriptionPlanRequest request);
    void deletePlan(Integer planId);
}