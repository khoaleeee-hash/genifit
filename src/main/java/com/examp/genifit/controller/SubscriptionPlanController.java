package com.examp.genifit.controller;

import com.examp.genifit.dto.request.CreateSubscriptionPlanRequest;
import com.examp.genifit.dto.request.UpdateSubscriptionPlanRequest;
import com.examp.genifit.dto.response.SubscriptionPlanResponse;
import com.examp.genifit.service.SubscriptionPlanService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subscription-plans")
@RequiredArgsConstructor
@Tag(name = "Subscription Plan")
public class SubscriptionPlanController {

    private final SubscriptionPlanService subscriptionPlanService;

    @GetMapping
    public List<SubscriptionPlanResponse> getActivePlans() {
        return subscriptionPlanService.getActivePlans();
    }

    @GetMapping("/{planId}")
    public SubscriptionPlanResponse getPlanById(
            @PathVariable Integer planId
    ) {
        return subscriptionPlanService.getPlanById(planId);
    }

    @PostMapping
    public SubscriptionPlanResponse createPlan(
            @RequestBody CreateSubscriptionPlanRequest request
    ) {
        return subscriptionPlanService.createPlan(request);
    }

    @PutMapping("/{planId}")
    public SubscriptionPlanResponse updatePlan(
            @PathVariable Integer planId,
            @RequestBody UpdateSubscriptionPlanRequest request
    ) {
        return subscriptionPlanService.updatePlan(planId, request);
    }

    @DeleteMapping("/{planId}")
    public String deletePlan(
            @PathVariable Integer planId
    ) {
        subscriptionPlanService.deletePlan(planId);
        return "Xoá gói đăng ký thành công";
    }
}