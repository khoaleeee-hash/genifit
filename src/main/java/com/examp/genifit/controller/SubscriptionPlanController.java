package com.examp.genifit.controller;

import com.examp.genifit.common.response.ApiResponse;
import com.examp.genifit.dto.request.CreateSubscriptionPlanRequest;
import com.examp.genifit.dto.request.UpdateSubscriptionPlanRequest;
import com.examp.genifit.dto.response.SubscriptionPlanResponse;
import com.examp.genifit.service.SubscriptionPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subscription-plans")
@RequiredArgsConstructor
@Tag(name = "Subscription Plan")
public class SubscriptionPlanController {

    private final SubscriptionPlanService subscriptionPlanService;

    @Operation(
            summary = "Lấy danh sách những gói đăng kí"
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<SubscriptionPlanResponse>>> getActivePlans(
            @RequestParam(defaultValue = "0") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        Page<SubscriptionPlanResponse> page =
                subscriptionPlanService.getActivePlans(pageNum, pageSize);

        return ResponseEntity.ok(
                ApiResponse.successPage("Get active subscription plans successfully", page)
        );
    }

    @Operation(
            summary = "Tìm gói đăng kí theo Id"
    )
    @GetMapping("/{planId}")
    public ResponseEntity<ApiResponse<SubscriptionPlanResponse>> getPlanById(
            @PathVariable Integer planId
    ) {
        SubscriptionPlanResponse response =
                subscriptionPlanService.getPlanById(planId);

        return ResponseEntity.ok(
                ApiResponse.success("Get subscription plan successfully", response)
        );
    }

    @Operation(
            summary = "Đăng kí gói"
    )
    @PostMapping
    public ResponseEntity<ApiResponse<SubscriptionPlanResponse>> createPlan(
            @RequestBody CreateSubscriptionPlanRequest request
    ) {
        SubscriptionPlanResponse response =
                subscriptionPlanService.createPlan(request);

        return ResponseEntity.ok(
                ApiResponse.success("Create subscription plan successfully", response)
        );
    }

    @Operation(
            summary = "Cập nhật gói đăng kí"
    )
    @PutMapping("/{planId}")
    public ResponseEntity<ApiResponse<SubscriptionPlanResponse>> updatePlan(
            @PathVariable Integer planId,
            @RequestBody UpdateSubscriptionPlanRequest request
    ) {
        SubscriptionPlanResponse response =
                subscriptionPlanService.updatePlan(planId, request);

        return ResponseEntity.ok(
                ApiResponse.success("Update subscription plan successfully", response)
        );
    }

    @Operation(
            summary = "Xoá gói đã đăng kí"
    )
    @DeleteMapping("/{planId}")
    public ResponseEntity<ApiResponse<String>> deletePlan(
            @PathVariable Integer planId
    ) {
        subscriptionPlanService.deletePlan(planId);

        return ResponseEntity.ok(
                ApiResponse.success("Delete subscription plan successfully", "Xoá gói đăng ký thành công")
        );
    }
}