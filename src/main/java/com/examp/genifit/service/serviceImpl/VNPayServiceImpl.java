package com.examp.genifit.service.serviceImpl;

import com.examp.genifit.entity.*;
import com.examp.genifit.repository.PaymentTransactionRepository;
import com.examp.genifit.repository.UserSubscriptionRepository;
import com.examp.genifit.service.VNPayService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
public class VNPayServiceImpl implements VNPayService {
    private final PaymentTransactionRepository transactionRepository;
    private final UserSubscriptionRepository subscriptionRepository;

    @Value("${vnpay.tmn-code}")
    private String tmnCode;

    @Value("${vnpay.hash-secret}")
    private String hashSecret;

    @Value("${vnpay.pay-url}")
    private String payUrl;

    @Value("${vnpay.ipn-url}")
    private String ipnUrl;

    @Value("${vnpay.redirect-url}")
    private String redirectUrl;

    @Value("${vnpay.version}")
    private String version;

    @Override
    public String createPayment(User user, SubscriptionPlan plan, String orderCode) {

        long amount = plan.getPrice().longValue()*100;
        String orderInfo = "ThanhToan-" + plan.getPlanName().replaceAll("\\s+", "") + "-GENEFIT";

        // 1. Build params — VNPay yêu cầu sort theo alphabet trước khi ký
        Map<String, String> params = new TreeMap<>(); // TreeMap tự sort key alphabet
        params.put("vnp_Version", version);
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", tmnCode);
        params.put("vnp_Amount", String.valueOf(amount)); // VNPay nhân 100
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", orderCode);
        params.put("vnp_OrderInfo", orderInfo);
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", redirectUrl);
        params.put("vnp_IpAddr", "127.0.0.1");
        params.put("vnp_CreateDate", getCurrentDateTime());
        params.put("vnp_ExpireDate", getExpireDateTime(15)); // hết hạn sau 15 phút

        // 2. Build query string và tính signature
        String queryString = buildQueryString(params, false);
        String signature = hmacSHA512(queryString, hashSecret);
        // Build URL với encoded string
        String encodedQuery = buildQueryString(params, true);

        System.out.println("=== VNPAY DEBUG ===");
        System.out.println("rawQuery (để ký): " + queryString);
        System.out.println("signature: " + signature);
        System.out.println("===================");

        return payUrl + "?" + encodedQuery + "&vnp_SecureHash=" + signature;
    }

    @Override
    @Transactional
    public void handleIPN(Map<String, String> ipnData) {
        // 1. Verify signature
        if (!verifySignature(ipnData)) {
            throw new RuntimeException("Invalid VNPay signature");
        }

        String orderCode = ipnData.get("vnp_TxnRef");
        String responseCode = ipnData.get("vnp_ResponseCode");
        String gatewayTransactionId = ipnData.get("vnp_TransactionNo");

        // 2. Tìm transaction
        PaymentTransaction transaction = transactionRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + orderCode));

        transaction.setGatewayTransactionId(gatewayTransactionId);
        transaction.setGatewayResponse(ipnData.toString());
        transaction.setUpdatedAt(LocalDateTime.now());

        if ("00".equals(responseCode)) {
            // "00" = thành công theo VNPay spec
            transaction.setStatus(PaymentTransaction.PaymentStatus.SUCCESS);
            transactionRepository.save(transaction);
            activateSubscription(transaction);
        } else {
            transaction.setStatus(PaymentTransaction.PaymentStatus.FAILED);
            transactionRepository.save(transaction);
        }
    }

    private boolean verifySignature(Map<String, String> ipnData) {
        String receivedHash = ipnData.get("vnp_SecureHash");

        // Loại bỏ các field signature trước khi tính lại
        Map<String, String> params = new TreeMap<>(ipnData);
        params.remove("vnp_SecureHash");
        params.remove("vnp_SecureHashType");

        String queryString = buildQueryString(params, false);
        String expectedHash = hmacSHA512(queryString, hashSecret);

        return expectedHash.equalsIgnoreCase(receivedHash);
    }

    private void activateSubscription(PaymentTransaction transaction) {
        User user = transaction.getUser();
        SubscriptionPlan plan = transaction.getPlan();

        UserSubscription subscription = subscriptionRepository.findByUser(user)
                .orElse(new UserSubscription());

        LocalDateTime startDate = LocalDateTime.now();
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



    // VNPay dùng HMAC-SHA512, khác MoMo dùng SHA256
    private String hmacSHA512(String data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException("HMAC-SHA512 error", e);
        }
    }

    private String buildQueryString(Map<String, String> params, boolean encode) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (sb.length() > 0) sb.append("&");
            if (encode) {
                sb.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
                sb.append("=");
                sb.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
            } else {
                sb.append(entry.getKey()).append("=").append(entry.getValue());
            }
        }
        return sb.toString();
    }

    private String getCurrentDateTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    private String getExpireDateTime(int minutes) {
        return LocalDateTime.now().plusMinutes(minutes)
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }
}
