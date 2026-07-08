package com.examp.genifit.service;

import com.examp.genifit.dto.request.CreateSubscriptionPlanRequest;
import com.examp.genifit.dto.request.SubscribePlanRequest;
import com.examp.genifit.dto.request.UpdateSubscriptionPlanRequest;
import com.examp.genifit.dto.response.MySubscriptionResponse;
import com.examp.genifit.dto.response.PageResponse;
import com.examp.genifit.dto.response.SubscriptionPlanResponse;
import com.examp.genifit.dto.response.UserSubscriptionResponse;

public interface SubscriptionPlanService {
    PageResponse<SubscriptionPlanResponse> getActivePlans(Integer pageNum, Integer pageSize);
    PageResponse<SubscriptionPlanResponse> getAllPlans(Integer pageNum, Integer pageSize);
    SubscriptionPlanResponse getPlanById(Integer planId);
    SubscriptionPlanResponse createPlan(CreateSubscriptionPlanRequest request);
    SubscriptionPlanResponse updatePlan(Integer planId, UpdateSubscriptionPlanRequest request);
    void deletePlan(Integer planId);
    MySubscriptionResponse getMySubscription(String username);
    UserSubscriptionResponse subscribePlan(String username, SubscribePlanRequest request);
}