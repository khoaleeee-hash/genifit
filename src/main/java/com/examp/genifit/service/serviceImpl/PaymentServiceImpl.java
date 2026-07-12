package com.examp.genifit.service.serviceImpl;

import com.examp.genifit.common.exception.ApiException;
import com.examp.genifit.common.exception.ErrorCode;
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
import com.examp.genifit.service.VNPayService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 50;

    private final UserRepository userRepository;
    private final SubscriptionPlanRepository planRepository;
    private final PaymentTransactionRepository transactionRepository;
    private final MoMoService moMoService;
    private final VNPayService vnPayService;

    @Override
    public PaymentResponseDto initPayment(
            String username,
            Integer planId,
            PaymentTransaction.PaymentMethod paymentMethod
    ) {
        User user = findUserByUsername(username);
        SubscriptionPlan plan = findPlanById(planId);

        validatePlanForPayment(plan);

        String orderCode = generateOrderCode(user.getUserId());

        createPendingTransaction(user, plan, orderCode, paymentMethod);

        String payUrl = callGateway(paymentMethod, user, plan, orderCode);

        return new PaymentResponseDto(orderCode, payUrl);
    }

    @Override
    public PaymentHistoryResponse getHistory(String username, Integer cursorId, int pageSize) {
        User user = findUserByUsername(username);

        int size = pageSize > 0 ? Math.min(pageSize, MAX_PAGE_SIZE) : DEFAULT_PAGE_SIZE;
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

    // =========================================================================
    // PRIVATE HELPERS
    // =========================================================================

    private String callGateway(
            PaymentTransaction.PaymentMethod paymentMethod,
            User user,
            SubscriptionPlan plan,
            String orderCode
    ) {
        return switch (paymentMethod) {
            case MOMO -> moMoService.createPayment(user, plan, orderCode);
            case VNPAY -> vnPayService.createPayment(user, plan, orderCode);
        };
    }

    private void createPendingTransaction(
            User user,
            SubscriptionPlan plan,
            String orderCode,
            PaymentTransaction.PaymentMethod paymentMethod
    ) {
        LocalDateTime now = LocalDateTime.now();

        PaymentTransaction transaction = PaymentTransaction.builder()
                .user(user)
                .plan(plan)
                .orderCode(orderCode)
                .paymentMethod(paymentMethod)
                .status(PaymentTransaction.PaymentStatus.PENDING)
                .amount(plan.getPrice())
                .createdAt(now)
                .updatedAt(now)
                .build();

        transactionRepository.save(transaction);
    }

    private String generateOrderCode(Integer userId) {
        return "GF" + System.currentTimeMillis() + userId;
    }

    private void validatePlanForPayment(SubscriptionPlan plan) {
        if (Boolean.TRUE.equals(plan.getDeleted())) {
            throw new ApiException(ErrorCode.SUBSCRIPTION_PLAN_NOT_FOUND, "Gói đăng ký đã bị xóa");
        }
        if (Boolean.FALSE.equals(plan.getActive())) {
            throw new ApiException(ErrorCode.SUBSCRIPTION_PLAN_INACTIVE, "Gói đăng ký chưa được kích hoạt");
        }
        if (plan.getPrice() == null || plan.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApiException(
                    ErrorCode.INVALID_SUBSCRIPTION_REQUEST,
                    "Gói miễn phí không cần thanh toán"
            );
        }
    }

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND, "Không tìm thấy người dùng"));
    }

    private SubscriptionPlan findPlanById(Integer planId) {
        return planRepository.findById(planId)
                .orElseThrow(() -> new ApiException(ErrorCode.SUBSCRIPTION_PLAN_NOT_FOUND, "Không tìm thấy gói đăng ký"));
    }
}