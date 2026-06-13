package com.examp.genifit.service;

import com.examp.genifit.dto.request.CreateSubscriptionPlanRequest;
import com.examp.genifit.dto.request.UpdateSubscriptionPlanRequest;
import com.examp.genifit.dto.response.SubscriptionPlanResponse;

import java.util.List;

public interface SubscriptionPlanService {
    List<SubscriptionPlanResponse> getActivePlans();
    SubscriptionPlanResponse getPlanById(Integer planId);
    SubscriptionPlanResponse createPlan(CreateSubscriptionPlanRequest request);
    SubscriptionPlanResponse updatePlan(Integer planId, UpdateSubscriptionPlanRequest request);
    void deletePlan(Integer planId);
}