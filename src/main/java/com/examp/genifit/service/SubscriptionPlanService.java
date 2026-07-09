package com.examp.genifit.service;

import com.examp.genifit.dto.request.CreateSubscriptionPlanRequest;
import com.examp.genifit.dto.request.SubscribePlanRequest;
import com.examp.genifit.dto.request.UpdateSubscriptionPlanRequest;
import com.examp.genifit.dto.response.MySubscriptionResponse;
import com.examp.genifit.dto.response.SubscriptionPlanResponse;
import com.examp.genifit.dto.response.UserSubscriptionResponse;
import org.springframework.data.domain.Page;

public interface SubscriptionPlanService {

    Page<SubscriptionPlanResponse> getAllPlans(
            Integer pageNum,
            Integer pageSize
    );

    Page<SubscriptionPlanResponse> getActivePlans(
            Integer pageNum,
            Integer pageSize
    );

    SubscriptionPlanResponse getPlanById(Integer planId);

    SubscriptionPlanResponse createPlan(
            CreateSubscriptionPlanRequest request
    );

    SubscriptionPlanResponse updatePlan(
            Integer planId,
            UpdateSubscriptionPlanRequest request
    );

    void deletePlan(Integer planId);

    MySubscriptionResponse getMySubscription(
            String username
    );

    UserSubscriptionResponse subscribePlan(
            String username,
            SubscribePlanRequest request
    );

    UserSubscriptionResponse cancelSubscription(String username);

    UserSubscriptionResponse renewSubscription(String username);

}