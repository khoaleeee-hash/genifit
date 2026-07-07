package com.examp.genifit.service.serviceImpl;

import com.examp.genifit.dto.response.PaymentHistoryResponse;
import com.examp.genifit.dto.response.PaymentResponseDto;
import com.examp.genifit.entity.PaymentTransaction;
import com.examp.genifit.entity.SubscriptionPlan;
import com.examp.genifit.entity.User;
import com.examp.genifit.repository.PaymentTransactionRepository;
import com.examp.genifit.repository.SubscriptionPlanRepository;
import com.examp.genifit.repository.UserRepository;
import com.examp.genifit.service.MoMoService;
import com.examp.genifit.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final UserRepository userRepository;
    private final SubscriptionPlanRepository planRepository;
    private final PaymentTransactionRepository transactionRepository;
    private final MoMoService moMoService;
    private static final int DEFAULT_PAGE_SIZE = 10;

    @Override
    public PaymentResponseDto initPayment(Integer userId, Integer planId, String paymentMethod) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        SubscriptionPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan not found"));


        // Sinh orderCode unique: timestamp + userId
        String orderCode = "GF" + System.currentTimeMillis() + userId;

        // Lưu transaction PENDING trước khi gọi cổng thanh toán
        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setUser(user);
        transaction.setPlan(plan);
        transaction.setOrderCode(orderCode);
        transaction.setPaymentMethod(PaymentTransaction.PaymentMethod.valueOf(paymentMethod));
        transaction.setStatus(PaymentTransaction.PaymentStatus.PENDING);
        transaction.setAmount(plan.getPrice());
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setUpdatedAt(LocalDateTime.now());
        transactionRepository.save(transaction);

        // Gọi cổng thanh toán tương ứng
        String payUrl = switch (paymentMethod) {
            case "MOMO" -> moMoService.createPayment(user, plan, orderCode);
            default -> throw new RuntimeException("Unsupported payment method: " + paymentMethod);
        };

        return new PaymentResponseDto(orderCode, payUrl);
    }



    @Override
    public PaymentHistoryResponse getHistory(Integer userId, Integer cursorId, int pageSize) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        int size = pageSize > 0 ? Math.min(pageSize, 50) : DEFAULT_PAGE_SIZE;
        Pageable pageable = PageRequest.of(0, size + 1); // lấy thừa 1 để biết còn data không

        List<PaymentTransaction> raw = (cursorId == null)
                ? transactionRepository.findByUserOrderByTransactionIdDesc(user, pageable)
                : transactionRepository.findByUserAndTransactionIdLessThanOrderByTransactionIdDesc(
                user, cursorId, pageable);

        boolean hasMore = raw.size() > size;
        if (hasMore) raw = raw.subList(0, size);

        List<PaymentHistoryResponse.PaymentHistoryDto> dtos = raw.stream()
                .map(t -> new PaymentHistoryResponse.PaymentHistoryDto(
                        t.getTransactionId(),
                        t.getOrderCode(),
                        t.getPlan().getPlanName(),
                        t.getAmount(),
                        t.getPaymentMethod().name(),
                        t.getStatus().name(),
                        t.getCreatedAt(),
                        t.getUpdatedAt()
                ))
                .toList();

        Integer nextCursor = hasMore ? raw.get(raw.size() - 1).getTransactionId() : null;

        return new PaymentHistoryResponse(dtos, nextCursor, hasMore);
    }
}
