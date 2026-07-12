package com.examp.genifit.service;

import com.examp.genifit.dto.request.CreateSubscriptionPlanRequest;
import com.examp.genifit.dto.request.SubscribePlanRequest;
import com.examp.genifit.dto.request.UpdateSubscriptionPlanRequest;
import com.examp.genifit.dto.response.*;
import org.springframework.data.domain.Page;

import java.util.List;

public interface SubscriptionPlanService {

    Page<SubscriptionPlanResponse> getAllPlans(Integer pageNum, Integer pageSize);

    Page<SubscriptionPlanResponse> getActivePlans(Integer pageNum, Integer pageSize);

    SubscriptionPlanResponse getPlanById(Integer planId);

    SubscriptionPlanResponse createPlan(CreateSubscriptionPlanRequest request);

    SubscriptionPlanResponse updatePlan(Integer planId, UpdateSubscriptionPlanRequest request);

    void deletePlan(Integer planId);

    MySubscriptionResponse getMySubscription(String username);

    List<UserSubscriptionResponse> getMySubscriptionHistory(String username);

    CancelSubscriptionResponse cancelSubscription(String username);

    SubscribePlanResponse subscribePlan(String username, SubscribePlanRequest request);

    SubscribePlanResponse renewSubscription(String username);
}