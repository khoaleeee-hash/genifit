package com.examp.genifit.controller;

import com.examp.genifit.dto.request.PaymentRequestDto;
import com.examp.genifit.dto.response.PaymentHistoryResponse;
import com.examp.genifit.dto.response.PaymentResponseDto;
import com.examp.genifit.service.MoMoService;
import com.examp.genifit.service.PaymentService;
import com.examp.genifit.service.VNPayService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;
    private final MoMoService moMoService;
    private final VNPayService vnPayService;

    // User bấm mua → nhận payUrl
    @PostMapping("/init")
    public ResponseEntity<PaymentResponseDto> initPayment(
            @RequestBody PaymentRequestDto request) {
        return ResponseEntity.ok(
                paymentService.initPayment(request.getUserId(), request.getPlanId(), request.getPaymentMethod())
        );
    }

    // MoMo callback về sau khi thanh toán
    @PostMapping("/momo/ipn")
    public ResponseEntity<String> momoIPN(@RequestBody Map<String, String> ipnData) {
        moMoService.handleIPN(ipnData);
        return ResponseEntity.ok("OK"); // MoMo yêu cầu phải trả "OK" để xác nhận đã nhận IPN
    }

    @GetMapping("/history/{userId}")
    public ResponseEntity<PaymentHistoryResponse> getHistory(
            @PathVariable Integer userId,
            @RequestParam(required = false) Integer cursor,
            @RequestParam(defaultValue = "10") int pageSize) {

        return ResponseEntity.ok(paymentService.getHistory(userId, cursor, pageSize));
    }

    // Endpoint này chỉ để redirect, không xử lý logic
    // Logic đã được xử lý ở IPN rồi
    @GetMapping("/momo/redirect")
    public ResponseEntity<String> momoRedirect(@RequestParam Map<String, String> params) {
        String resultCode = params.get("resultCode");
        String orderId = params.get("orderId");

        // "0" = thành công theo MoMo spec
        if ("0".equals(resultCode)) {
            return ResponseEntity.ok("Thanh toán thành công! OrderId: " + orderId);
        } else {
            return ResponseEntity.ok("Thanh toán thất bại hoặc bị huỷ.");
        }
    }

    // VNPay IPN — nhận query params (khác MoMo nhận JSON)
    @GetMapping("/vnpay/ipn")
    public ResponseEntity<Map<String, String>> vnpayIPN(
            @RequestParam Map<String, String> params) {
        vnPayService.handleIPN(params);
        // VNPay yêu cầu trả về JSON này để xác nhận đã nhận IPN
        return ResponseEntity.ok(Map.of(
                "RspCode", "00",
                "Message", "Confirm Success"
        ));
    }

    @GetMapping("/vnpay/redirect")
    public ResponseEntity<String> vnpayRedirect(@RequestParam Map<String, String> params) {
        return "00".equals(params.get("vnp_ResponseCode"))
                ? ResponseEntity.ok("Thanh toán VNPay thành công!")
                : ResponseEntity.ok("Thanh toán VNPay thất bại hoặc bị huỷ.");
    }
}
