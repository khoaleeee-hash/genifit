package com.examp.genifit.controller;

import com.examp.genifit.common.response.ApiResponse;
import com.examp.genifit.dto.request.CreateSubscriptionPlanRequest;
import com.examp.genifit.dto.request.SubscribePlanRequest;
import com.examp.genifit.dto.request.UpdateSubscriptionPlanRequest;
import com.examp.genifit.dto.response.MySubscriptionResponse;
import com.examp.genifit.dto.response.SubscriptionPlanResponse;
import com.examp.genifit.dto.response.UserSubscriptionResponse;
import com.examp.genifit.service.SubscriptionPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionPlanController {

    private final SubscriptionPlanService subscriptionPlanService;

    // =========================================================================
    // PLAN MANAGEMENT
    // =========================================================================

    @GetMapping("/plans")
    public ResponseEntity<ApiResponse<java.util.List<SubscriptionPlanResponse>>> getAllPlans(
            @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer pageSize
    ) {
        Page<SubscriptionPlanResponse> page = subscriptionPlanService.getAllPlans(pageNum, pageSize);
        return ResponseEntity.ok(ApiResponse.successPage("Lấy danh sách gói thành công", page));
    }

    @GetMapping("/plans/active")
    public ResponseEntity<ApiResponse<java.util.List<SubscriptionPlanResponse>>> getActivePlans(
            @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer pageSize
    ) {
        Page<SubscriptionPlanResponse> page = subscriptionPlanService.getActivePlans(pageNum, pageSize);
        return ResponseEntity.ok(ApiResponse.successPage("Lấy danh sách gói active thành công", page));
    }

    @GetMapping("/plans/{planId}")
    public ResponseEntity<ApiResponse<SubscriptionPlanResponse>> getPlanById(
            @PathVariable Integer planId
    ) {
        SubscriptionPlanResponse response = subscriptionPlanService.getPlanById(planId);
        return ResponseEntity.ok(ApiResponse.success("Lấy gói thành công", response));
    }

    @PostMapping("/plans")
    public ResponseEntity<ApiResponse<SubscriptionPlanResponse>> createPlan(
            @RequestBody CreateSubscriptionPlanRequest request
    ) {
        SubscriptionPlanResponse response = subscriptionPlanService.createPlan(request);
        return ResponseEntity.status(201).body(ApiResponse.success("Tạo gói thành công", response));
    }

    @PutMapping("/plans/{planId}")
    public ResponseEntity<ApiResponse<SubscriptionPlanResponse>> updatePlan(
            @PathVariable Integer planId,
            @RequestBody UpdateSubscriptionPlanRequest request
    ) {
        SubscriptionPlanResponse response = subscriptionPlanService.updatePlan(planId, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật gói thành công", response));
    }

    @DeleteMapping("/plans/{planId}")
    public ResponseEntity<ApiResponse<Void>> deletePlan(
            @PathVariable Integer planId
    ) {
        subscriptionPlanService.deletePlan(planId);
        return ResponseEntity.ok(ApiResponse.success("Xóa gói thành công", null));
    }

    // =========================================================================
    // USER SUBSCRIPTION
    // =========================================================================

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MySubscriptionResponse>> getMySubscription(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        MySubscriptionResponse response = subscriptionPlanService.getMySubscription(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Lấy gói đăng ký thành công", response));
    }

    @PostMapping("/subscribe")
    public ResponseEntity<ApiResponse<UserSubscriptionResponse>> subscribePlan(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody SubscribePlanRequest request
    ) {
        UserSubscriptionResponse response = subscriptionPlanService.subscribePlan(
                userDetails.getUsername(), request
        );
        return ResponseEntity.ok(ApiResponse.success("Đăng ký gói thành công", response));
    }

    @PostMapping("/cancel")
    public ResponseEntity<ApiResponse<UserSubscriptionResponse>> cancelSubscription(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UserSubscriptionResponse response = subscriptionPlanService.cancelSubscription(
                userDetails.getUsername()
        );
        return ResponseEntity.ok(ApiResponse.success("Hủy gói thành công", response));
    }

    @PostMapping("/renew")
    public ResponseEntity<ApiResponse<UserSubscriptionResponse>> renewSubscription(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UserSubscriptionResponse response = subscriptionPlanService.renewSubscription(
                userDetails.getUsername()
        );
        return ResponseEntity.ok(ApiResponse.success("Gia hạn gói thành công", response));
    }
}