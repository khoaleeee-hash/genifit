package com.examp.genifit.service.serviceImpl;

import com.examp.genifit.entity.*;
import com.examp.genifit.repository.PaymentTransactionRepository;
import com.examp.genifit.repository.SubscriptionPlanRepository;
import com.examp.genifit.repository.UserSubscriptionRepository;
import com.examp.genifit.service.MoMoService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MoMoServiceImpl implements MoMoService {
    private final WebClient.Builder webClientBuilder;
    private final PaymentTransactionRepository transactionRepository;
    private final UserSubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository planRepository;

    @Value("${momo.partner-code}")
    private String partnerCode;

    @Value("${momo.access-key}")
    private String accessKey;

    @Value("${momo.secret-key}")
    private String secretKey;

    @Value("${momo.api-url}")
    private String apiUrl;

    @Value("${momo.ipn-url}")
    private String ipnUrl;

    @Value("${momo.redirect-url}")
    private String redirectUrl;

    @Override
    public String createPayment(User user, SubscriptionPlan plan, String orderCode) {
        String requestId = UUID.randomUUID().toString();
        String orderInfo = "Mua " + plan.getPlanName() + " - GENEFIT";
        long amount = plan.getPrice().longValue();

        String rawSignature = "accessKey=" + accessKey
                + "&amount=" + amount
                + "&extraData="
                + "&ipnUrl=" + ipnUrl
                + "&orderId=" + orderCode
                + "&orderInfo=" + orderInfo
                + "&partnerCode=" + partnerCode
                + "&redirectUrl=" + redirectUrl
                + "&requestId=" + requestId
                + "&requestType=payWithMethod";

        String signature = hmacSHA256(rawSignature, secretKey);


        // 2. Build request body
        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("partnerCode", partnerCode);
        requestBody.put("accessKey", accessKey);
        requestBody.put("requestId", requestId);
        requestBody.put("amount", amount);
        requestBody.put("orderId", orderCode);
        requestBody.put("orderInfo", orderInfo);
        requestBody.put("redirectUrl", redirectUrl);
        requestBody.put("ipnUrl", ipnUrl);
        requestBody.put("extraData", "");
        requestBody.put("requestType", "payWithMethod");
        requestBody.put("signature", signature);
        requestBody.put("lang", "vi");

        System.out.println("=== MOMO DEBUG ===");
        System.out.println("partnerCode: " + partnerCode);
        System.out.println("accessKey: " + accessKey);
        System.out.println("secretKey: " + secretKey);
        System.out.println("rawSignature: " + rawSignature);
        System.out.println("signature: " + signature);
        System.out.println("requestBody: " + requestBody);
        System.out.println("==================");

        // 3. Gọi MoMo API
        Map response = webClientBuilder.build()
                .post()
                .uri(apiUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .doOnNext(body -> System.out.println("MoMo error response: " + body))
                                .map(body -> new RuntimeException("MoMo 400: " + body))
                )
                .bodyToMono(Map.class)
                .block();

        String payUrl = (String) response.get("payUrl");
        if (payUrl == null) {
            throw new RuntimeException("Không thể tạo link thanh toán MoMo");
        }

        return payUrl;
    }

    @Override
    public void handleIPN(Map<String, String> ipnData) {
        String orderCode = ipnData.get("orderId");
        String resultCode = ipnData.get("resultCode");
        String gatewayTransactionId = ipnData.get("transId");
        String rawResponse = ipnData.toString();

        // 1. Verify signature để chắc chắn request đến từ MoMo, không phải giả mạo
        if (!verifySignature(ipnData)) {
            throw new RuntimeException("Invalid MoMo signature");
        }

        // 2. Tìm transaction trong DB
        PaymentTransaction transaction = transactionRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + orderCode));

        // 3. Cập nhật transaction
        transaction.setGatewayTransactionId(gatewayTransactionId);
        transaction.setGatewayResponse(rawResponse);
        transaction.setUpdatedAt(LocalDateTime.now());

        if ("0".equals(resultCode)) {
            // Thanh toán thành công
            transaction.setStatus(PaymentTransaction.PaymentStatus.SUCCESS);
            transactionRepository.save(transaction);

            // 4. Tạo hoặc gia hạn UserSubscription
            activateSubscription(transaction);
        } else {
            // Thanh toán thất bại
            transaction.setStatus(PaymentTransaction.PaymentStatus.FAILED);
            transactionRepository.save(transaction);
        }
    }

    private void activateSubscription(PaymentTransaction transaction) {
        User user = transaction.getUser();
        SubscriptionPlan plan = transaction.getPlan();

        // Kiểm tra đã có subscription chưa
        UserSubscription subscription = subscriptionRepository.findByUser(user)
                .orElse(new UserSubscription());

        LocalDateTime startDate = LocalDateTime.now();

        // Nếu đang còn hạn thì cộng thêm, không tính lại từ đầu
        if (subscription.getEndDate() != null && subscription.getEndDate().isAfter(startDate)) {
            startDate = subscription.getEndDate();
        }

        subscription.setUser(user);
        subscription.setSubscriptionPlan(plan);
        subscription.setTransaction(transaction);
        subscription.setStartDate(LocalDateTime.now());
        subscription.setEndDate(startDate.plusDays(plan.getDurationDays()));
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setUpdatedAt(LocalDateTime.now());

        if (subscription.getCreatedAt() == null) {
            subscription.setCreatedAt(LocalDateTime.now());
        }

        subscriptionRepository.save(subscription);
    }

    private boolean verifySignature(Map<String, String> ipnData) {
        String rawSignature = "accessKey=" + accessKey
                + "&amount=" + ipnData.get("amount")
                + "&extraData=" + ipnData.get("extraData")
                + "&message=" + ipnData.get("message")
                + "&orderId=" + ipnData.get("orderId")
                + "&orderInfo=" + ipnData.get("orderInfo")
                + "&orderType=" + ipnData.get("orderType")
                + "&partnerCode=" + ipnData.get("partnerCode")
                + "&payType=" + ipnData.get("payType")
                + "&requestId=" + ipnData.get("requestId")
                + "&responseTime=" + ipnData.get("responseTime")
                + "&resultCode=" + ipnData.get("resultCode")
                + "&transId=" + ipnData.get("transId");

        String expectedSignature = hmacSHA256(rawSignature, secretKey);
        return expectedSignature.equals(ipnData.get("signature"));
    }

    private String hmacSHA256(String data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException("HMAC-SHA256 error", e);
        }
    }
}
