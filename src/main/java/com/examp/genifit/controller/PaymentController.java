package com.examp.genifit.controller;

import com.examp.genifit.common.exception.ApiException;
import com.examp.genifit.dto.request.PaymentRequestDto;
import com.examp.genifit.dto.response.PaymentHistoryResponse;
import com.examp.genifit.dto.response.PaymentResponseDto;
import com.examp.genifit.service.MoMoService;
import com.examp.genifit.service.PaymentService;
import com.examp.genifit.service.VNPayService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Tag(name = "Payment")
public class PaymentController {

    private final PaymentService paymentService;
    private final MoMoService moMoService;
    private final VNPayService vnPayService;

    // =========================================================================
    // INIT PAYMENT
    // =========================================================================

    @Operation(
            summary = "Khởi tạo thanh toán cho 1 gói subscription",
            description = """
                    User được xác định qua token đăng nhập (Bearer token), **không cần** truyền `userId`.

                    Request:
                    ```json
                    { "planId": 2, "paymentMethod": "VNPAY" }
                    ```

                    Response:
                    ```json
                    {
                      "orderCode": "GF17123456789005",
                      "payUrl": "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?..."
                    }
                    ```
                    → FE redirect user sang `payUrl` ngay sau khi nhận response (VD: `window.location.href = payUrl`).
                    """
    )
    @PostMapping("/init")
    public ResponseEntity<PaymentResponseDto> initPayment(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody PaymentRequestDto request
    ) {
        String username = jwt.getSubject();

        PaymentResponseDto response = paymentService.initPayment(
                username,
                request.getPlanId(),
                request.getPaymentMethod()
        );

        return ResponseEntity.ok(response);
    }
    // =========================================================================
    // PAYMENT HISTORY
    // =========================================================================

    @Operation(
            summary = "Lấy lịch sử giao dịch thanh toán của user đang đăng nhập",
            description = """
                    Dùng **cursor-based pagination**:
                    - Lần gọi đầu tiên: **không truyền** `cursor`.
                    - Các lần sau: truyền `cursor = nextCursor` nhận được ở response trước đó.
                    - `hasMore = false` nghĩa là đã lấy hết dữ liệu, không cần gọi thêm.
                    """
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/history")
    public ResponseEntity<PaymentHistoryResponse> getHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Cursor (transactionId) để lấy trang tiếp theo, bỏ trống ở lần gọi đầu tiên")
            @RequestParam(required = false) Integer cursor,
            @Parameter(description = "Số item mỗi trang, tối đa 50", example = "10")
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        return ResponseEntity.ok(
                paymentService.getHistory(userDetails.getUsername(), cursor, pageSize)
        );
    }


    // =========================================================================
    // MOMO REDIRECT (trình duyệt tự điều hướng, KHÔNG phải API cho FE gọi bằng AJAX)
    // =========================================================================

    @Hidden
    @Operation(
            summary = "[BROWSER REDIRECT] MoMo chuyển hướng trình duyệt user về sau khi thanh toán",
            description = """
                    Đây là `redirectUrl` cấu hình cho MoMo — trình duyệt tự động điều hướng tới đây,
                    KHÔNG phải API để FE gọi bằng fetch/axios.
                    Logic xác nhận thanh toán thực tế đã xử lý ở IPN (`/momo/ipn`);
                    endpoint này chỉ hiển thị thông báo tạm thời cho user xem trên trình duyệt.
                    """
    )
    @GetMapping("/momo/redirect")
    public ResponseEntity<Void> momoRedirect(@RequestParam Map<String, String> params) {
        String resultCode = params.get("resultCode");

        if ("0".equals(resultCode)) {
            moMoService.handleIPN(params);
            // Redirect về Gmail để user xem hóa đơn
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create("https://mail.google.com/mail/u/0/#inbox"))
                    .build();
        }

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("https://mail.google.com/mail/u/0/#inbox"))
                .build();
    }


    @Hidden
    @Operation(
            summary = "[BROWSER REDIRECT] VNPay chuyển hướng trình duyệt user về sau khi thanh toán",
            description = "Tương tự momoRedirect — không phải API cho FE gọi trực tiếp."
    )
    @GetMapping("/vnpay/redirect")
    public ResponseEntity<String> vnpayRedirect(@RequestParam Map<String, String> params) {
        String responseCode = params.get("vnp_ResponseCode");

        if (!"00".equals(responseCode)) {
            return ResponseEntity.ok("Thanh toán thất bại hoặc bị huỷ.");
        }


        vnPayService.handleIPN(params);

        return ResponseEntity.ok("Thanh toán thành công! Gói Premium đã được kích hoạt.");
    }
}