package com.examp.genifit.controller;

import com.examp.genifit.common.response.ApiResponse;
import com.examp.genifit.dto.request.CreateSubscriptionPlanRequest;
import com.examp.genifit.dto.request.SubscribePlanRequest;
import com.examp.genifit.dto.request.UpdateSubscriptionPlanRequest;
import com.examp.genifit.dto.response.*;
import com.examp.genifit.service.SubscriptionPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
@Tag(name = "Subscription")
public class SubscriptionPlanController {

    private final SubscriptionPlanService subscriptionPlanService;

    // =========================================================================
    // PLAN MANAGEMENT
    // =========================================================================

    @Operation(
            summary = "Lấy danh sách tất cả gói đăng ký (kể cả inactive)",
            description = """
                    Dùng cho trang **quản trị (admin)**. Trả về cả những gói đã bị `active = false`,
                    nhưng KHÔNG bao gồm gói đã bị xóa mềm (`deleted = true`).

                    Mặc định: `pageNum = 1`, `pageSize = 10` (tối đa `pageSize = 100`).
                    Kết quả luôn sắp xếp theo `price ASC`.
                    """
    )
    @GetMapping("/plans")
    public ResponseEntity<ApiResponse<List<SubscriptionPlanResponse>>> getAllPlans(
            @Parameter(description = "Số trang, bắt đầu từ 1", example = "1")
            @RequestParam(required = false) Integer pageNum,
            @Parameter(description = "Số item mỗi trang, tối đa 100", example = "10")
            @RequestParam(required = false) Integer pageSize
    ) {
        Page<SubscriptionPlanResponse> page = subscriptionPlanService.getAllPlans(pageNum, pageSize);
        return ResponseEntity.ok(ApiResponse.successPage("Lấy danh sách gói thành công", page));
    }

    @Operation(
            summary = "Lấy danh sách gói đang mở bán (active)",
            description = """
                    Dùng cho **trang bảng giá (pricing page)** hiển thị cho user.
                    Chỉ trả về gói có `active = true` và `deleted = false`, sắp xếp theo giá tăng dần.
                    Đây là API mà FE nên gọi khi render danh sách gói cho user chọn mua.
                    """
    )
    @GetMapping("/plans/active")
    public ResponseEntity<ApiResponse<List<SubscriptionPlanResponse>>> getActivePlans(
            @Parameter(description = "Số trang, bắt đầu từ 1", example = "1")
            @RequestParam(required = false) Integer pageNum,
            @Parameter(description = "Số item mỗi trang, tối đa 100", example = "10")
            @RequestParam(required = false) Integer pageSize
    ) {
        Page<SubscriptionPlanResponse> page = subscriptionPlanService.getActivePlans(pageNum, pageSize);
        return ResponseEntity.ok(ApiResponse.successPage("Lấy danh sách gói active thành công", page));
    }

    @Operation(summary = "Lấy chi tiết 1 gói đăng ký theo ID")
    @GetMapping("/plans/{planId}")
    public ResponseEntity<ApiResponse<SubscriptionPlanResponse>> getPlanById(
            @Parameter(description = "ID của gói đăng ký", required = true, example = "1")
            @PathVariable Integer planId
    ) {
        SubscriptionPlanResponse response = subscriptionPlanService.getPlanById(planId);
        return ResponseEntity.ok(ApiResponse.success("Lấy gói thành công", response));
    }

    @Operation(
            summary = "[ADMIN] Tạo gói đăng ký mới",
            description = "Yêu cầu quyền admin. `planType` và `planName` phải unique trong các gói chưa bị xóa."
    )
    @PostMapping("/plans")
    public ResponseEntity<ApiResponse<SubscriptionPlanResponse>> createPlan(
            @RequestBody CreateSubscriptionPlanRequest request
    ) {
        SubscriptionPlanResponse response = subscriptionPlanService.createPlan(request);
        return ResponseEntity.status(201).body(ApiResponse.success("Tạo gói thành công", response));
    }

    @Operation(
            summary = "[ADMIN] Cập nhật thông tin gói đăng ký",
            description = "Chỉ update các field được truyền lên (null field sẽ bị bỏ qua, giữ nguyên giá trị cũ)."
    )
    @PutMapping("/plans/{planId}")
    public ResponseEntity<ApiResponse<SubscriptionPlanResponse>> updatePlan(
            @Parameter(description = "ID của gói cần cập nhật", required = true)
            @PathVariable Integer planId,
            @RequestBody UpdateSubscriptionPlanRequest request
    ) {
        SubscriptionPlanResponse response = subscriptionPlanService.updatePlan(planId, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật gói thành công", response));
    }

    @Operation(
            summary = "[ADMIN] Xóa mềm gói đăng ký",
            description = """
                    Đây là **xóa mềm** (soft delete): set `deleted = true` và `active = false`.
                    Gói vẫn tồn tại trong DB để giữ lịch sử cho các subscription cũ đã tham chiếu tới nó.
                    """
    )
    @DeleteMapping("/plans/{planId}")
    public ResponseEntity<ApiResponse<Void>> deletePlan(
            @Parameter(description = "ID của gói cần xóa", required = true)
            @PathVariable Integer planId
    ) {
        subscriptionPlanService.deletePlan(planId);
        return ResponseEntity.ok(ApiResponse.success("Xóa gói thành công", null));
    }

    // =========================================================================
    // USER SUBSCRIPTION
    // =========================================================================

    @Operation(
            summary = "Lấy thông tin gói đăng ký hiện tại của user",
            description = """
                    Trả về subscription đang ở trạng thái `ACTIVE` gần nhất của user đang đăng nhập,
                    kèm đầy đủ quyền lợi (limit, feature flags...) của gói đó.

                    **Dùng để polling sau khi thanh toán**: sau khi user redirect về từ VNPay/MoMo,
                    FE gọi API này để kiểm tra subscription đã được kích hoạt (IPN đã xử lý xong) hay chưa.

                    Nếu chưa có subscription ACTIVE nào (kể cả free), trả về lỗi 404 `SUB_002`.
                    """
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MySubscriptionResponse>> getMySubscription(
            Principal principal // ĐÃ SỬA
    ) {
        MySubscriptionResponse response = subscriptionPlanService.getMySubscription(principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Lấy gói đăng ký thành công", response));
    }

    @Operation(summary = "Hủy gói đăng ký đang active (có tính hoàn tiền)")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/cancel")
    public ResponseEntity<ApiResponse<CancelSubscriptionResponse>> cancelSubscription(
            Principal principal // ĐÃ SỬA
    ) {
        CancelSubscriptionResponse response = subscriptionPlanService.cancelSubscription(
                principal.getName()
        );
        return ResponseEntity.ok(ApiResponse.success("Hủy gói thành công", response));
    }

    @Operation(
            summary = "Gia hạn gói đang active (không cần chọn lại planId)",
            description = """
                    Tự động lấy gói hiện tại (`subscriptionPlan`) của subscription đang `ACTIVE` để gia hạn thêm
                    `durationDays` ngày, tính từ `endDate` hiện tại (nếu chưa hết hạn) hoặc từ thời điểm hiện tại
                    (nếu đã hết hạn).

                    **Cùng cơ chế 2 kịch bản như `/subscribe`**:
                    - Gói free → gia hạn ngay, trả `requiresPayment: false`.
                    - Gói trả phí → trả `requiresPayment: true` + `paymentUrl`, FE redirect user đi thanh toán.
                      Phương thức thanh toán sẽ **tự động dùng lại phương thức của lần thanh toán gần nhất**
                      (VNPay hoặc MoMo), fallback về VNPay nếu chưa có lịch sử thanh toán.
                    """
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/renew")
    public ResponseEntity<ApiResponse<SubscribePlanResponse>> renewSubscription(
            Principal principal
    ) {
        SubscribePlanResponse response = subscriptionPlanService.renewSubscription(
                principal.getName()
        );

        String message = response.isRequiresPayment()
                ? "Vui lòng thanh toán để gia hạn gói"
                : "Gia hạn gói thành công";

        return ResponseEntity.ok(ApiResponse.success(message, response));
    }

    @Operation(summary = "Lấy lịch sử tất cả gói đăng ký (kể cả đã hết hạn/hủy) của user")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<UserSubscriptionResponse>>> getMySubscriptionHistory(
            Principal principal
    ) {
        List<UserSubscriptionResponse> response =
                subscriptionPlanService.getMySubscriptionHistory(principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Lấy lịch sử gói đăng ký thành công", response));
    }
}