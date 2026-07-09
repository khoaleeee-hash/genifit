package com.examp.genifit.service.serviceImpl;

import com.examp.genifit.common.exception.ApiException;
import com.examp.genifit.common.exception.ErrorCode;
import com.examp.genifit.dto.request.CreateSubscriptionPlanRequest;
import com.examp.genifit.dto.request.SubscribePlanRequest;
import com.examp.genifit.dto.request.UpdateSubscriptionPlanRequest;
import com.examp.genifit.dto.response.*;
import com.examp.genifit.entity.*;
import com.examp.genifit.mapper.SubscriptionPlanMapper;
import com.examp.genifit.mapper.UserSubscriptionMapper;
import com.examp.genifit.repository.SubscriptionPlanRepository;
import com.examp.genifit.repository.UserRepository;
import com.examp.genifit.repository.UserSubscriptionRepository;
import com.examp.genifit.service.MoMoService;
import com.examp.genifit.service.PaymentService;
import com.examp.genifit.service.SubscriptionPlanService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class SubscriptionPlanServiceImpl implements SubscriptionPlanService {

    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final UserRepository userRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final SubscriptionPlanMapper subscriptionPlanMapper;
    private final UserSubscriptionMapper userSubscriptionMapper;
    private final PaymentService paymentService;
    private final MoMoService moMoService;

    // =========================================================================
    // PUBLIC API - PLAN MANAGEMENT
    // =========================================================================

    @Override
    public Page<SubscriptionPlanResponse> getAllPlans(Integer pageNum, Integer pageSize) {
        Pageable pageable = buildPageable(pageNum, pageSize);
        return subscriptionPlanRepository
                .findByDeletedFalse(pageable)
                .map(subscriptionPlanMapper::toResponse);
    }

    @Override
    public Page<SubscriptionPlanResponse> getActivePlans(Integer pageNum, Integer pageSize) {
        Pageable pageable = buildPageable(pageNum, pageSize);
        return subscriptionPlanRepository
                .findByActiveTrueAndDeletedFalseOrderByPriceAsc(pageable)
                .map(subscriptionPlanMapper::toResponse);
    }

    @Override
    public SubscriptionPlanResponse getPlanById(Integer planId) {
        return subscriptionPlanMapper.toResponse(findPlanById(planId));
    }

    @Override
    public SubscriptionPlanResponse createPlan(CreateSubscriptionPlanRequest request) {
        validateCreateRequest(request);

        if (subscriptionPlanRepository.existsByPlanTypeAndDeletedFalse(request.getPlanType())) {
            throw new ApiException(ErrorCode.SUBSCRIPTION_PLAN_EXISTED, "Plan type đã tồn tại");
        }

        SubscriptionPlan plan = subscriptionPlanMapper.toEntity(request);
        applyDefaults(plan, request);

        return subscriptionPlanMapper.toResponse(subscriptionPlanRepository.save(plan));
    }

    @Override
    public SubscriptionPlanResponse updatePlan(Integer planId, UpdateSubscriptionPlanRequest request) {
        SubscriptionPlan plan = findPlanById(planId);

        if (request.getPlanType() != null && !request.getPlanType().equals(plan.getPlanType())) {
            if (subscriptionPlanRepository.existsByPlanTypeAndDeletedFalse(request.getPlanType())) {
                throw new ApiException(ErrorCode.SUBSCRIPTION_PLAN_EXISTED, "Plan type đã tồn tại");
            }
        }

        subscriptionPlanMapper.updateEntity(request, plan);

        return subscriptionPlanMapper.toResponse(subscriptionPlanRepository.save(plan));
    }

    @Override
    public void deletePlan(Integer planId) {
        SubscriptionPlan plan = findPlanById(planId);
        plan.setDeleted(true);
        plan.setActive(false);
        subscriptionPlanRepository.save(plan);
    }

    // =========================================================================
    // PUBLIC API - USER SUBSCRIPTION
    // =========================================================================

    @Override
    public MySubscriptionResponse getMySubscription(String username) {
        User user = findUserByUsername(username);
        UserSubscription subscription = findActiveSubscription(user);
        return userSubscriptionMapper.toMySubscription(subscription);
    }

    @Override
    public List<UserSubscriptionResponse> getMySubscriptionHistory(String username) {
        User user = findUserByUsername(username);

        return userSubscriptionRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(userSubscriptionMapper::toResponse)
                .toList();
    }

    @Override
    public SubscribePlanResponse subscribePlan(String username, SubscribePlanRequest request) {
        User user = findUserByUsername(username);
        SubscriptionPlan plan = findPlanById(request.getPlanId());

        validatePlanForSubscription(plan);

        if (isPaidPlan(plan)) {
            PaymentTransaction.PaymentMethod method = resolvePaymentMethod(request.getPaymentMethod());
            return initiatePaidSubscription(username, plan, method);
        }

        UserSubscriptionResponse response = processFreeSubscription(user, plan, request);

        return SubscribePlanResponse.builder()
                .requiresPayment(false)
                .subscription(response)
                .build();
    }

    @Override
    public CancelSubscriptionResponse cancelSubscription(String username) {
        User user = findUserByUsername(username);
        UserSubscription subscription = findActiveSubscription(user);

        LocalDateTime now = LocalDateTime.now();
        RefundResult refundResult = calculateRefund(subscription, now);

        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscription.setCancelledAt(now);
        subscription.setAutoRenew(false);

        subscription.setRefundStatus(refundResult.refundStatus());
        subscription.setRefundAmount(refundResult.refundAmount());
        subscription.setRefundPercent(refundResult.refundPercent());
        subscription.setRefundReason(refundResult.message());

        if (refundResult.refundStatus() == RefundStatus.PENDING) {
            processRefund(subscription, refundResult, now);
        }

        UserSubscription saved = userSubscriptionRepository.save(subscription);

        return CancelSubscriptionResponse.builder()
                .subscriptionId(saved.getSubscriptionId())
                .subscriptionStatus(saved.getStatus().name())
                .startDate(saved.getStartDate())
                .endDate(saved.getEndDate())
                .cancelledAt(saved.getCancelledAt())
                .usedDays(refundResult.usedDays())
                .refundEligible(saved.getRefundAmount() != null
                        && saved.getRefundAmount().compareTo(BigDecimal.ZERO) > 0)
                .refundStatus(saved.getRefundStatus())
                .refundPercent(saved.getRefundPercent())
                .refundAmount(saved.getRefundAmount())
                .refundMessage(saved.getRefundReason())
                .build();
    }

    @Override
    public SubscribePlanResponse renewSubscription(String username) {
        User user = findUserByUsername(username);
        UserSubscription subscription = findActiveSubscription(user);
        SubscriptionPlan plan = subscription.getSubscriptionPlan();

        validatePlanForSubscription(plan);

        if (isPaidPlan(plan)) {
            PaymentTransaction.PaymentMethod method = resolveRenewPaymentMethod(subscription);
            return initiatePaidSubscription(username, plan, method);
        }

        subscription.setEndDate(
                calculateRenewEndDate(subscription.getEndDate(), plan.getDurationDays())
        );

        UserSubscriptionResponse response =
                userSubscriptionMapper.toResponse(userSubscriptionRepository.save(subscription));

        return SubscribePlanResponse.builder()
                .requiresPayment(false)
                .subscription(response)
                .build();
    }

    // =========================================================================
    // PRIVATE - REFUND HELPERS (port từ UserServiceImpl)
    // =========================================================================

    private void processRefund(
            UserSubscription subscription,
            RefundResult refundResult,
            LocalDateTime now
    ) {
        subscription.setRefundRequestedAt(now);

        PaymentTransaction transaction = subscription.getTransaction();

        if (transaction == null) {
            subscription.setRefundStatus(RefundStatus.REJECTED);
            subscription.setRefundReason("Không tìm thấy giao dịch thanh toán gốc để hoàn tiền");
            return;
        }

        if (transaction.getPaymentMethod() != PaymentTransaction.PaymentMethod.MOMO) {
            subscription.setRefundStatus(RefundStatus.REJECTED);
            subscription.setRefundReason("Giao dịch không phải MoMo, không thể hoàn tiền qua MoMo");
            return;
        }

        if (transaction.getGatewayTransactionId() == null
                || transaction.getGatewayTransactionId().isBlank()) {
            subscription.setRefundStatus(RefundStatus.REJECTED);
            subscription.setRefundReason("Không tìm thấy mã giao dịch MoMo để hoàn tiền");
            return;
        }

        MoMoRefundResponse momoRefundResponse = moMoService.refund(
                transaction.getOrderCode(),
                transaction.getGatewayTransactionId(),
                refundResult.refundAmount(),
                refundResult.message()
        );

        if (momoRefundResponse.isSuccess()) {
            subscription.setRefundStatus(RefundStatus.COMPLETED);
            subscription.setRefundCompletedAt(LocalDateTime.now());
            subscription.setRefundTransactionId(momoRefundResponse.getRefundTransactionId());
            subscription.setRefundReason("Hoàn tiền MoMo thành công: " + momoRefundResponse.getMessage());
        } else if (momoRefundResponse.isPending()) {
            subscription.setRefundStatus(RefundStatus.PENDING);
            subscription.setRefundTransactionId(momoRefundResponse.getRefundTransactionId());
            subscription.setRefundReason("MoMo đang xử lý hoàn tiền: " + momoRefundResponse.getMessage());
        } else {
            subscription.setRefundStatus(RefundStatus.REJECTED);
            subscription.setRefundTransactionId(momoRefundResponse.getRefundTransactionId());
            subscription.setRefundReason("Hoàn tiền MoMo thất bại: " + momoRefundResponse.getMessage());
        }
    }

    private RefundResult calculateRefund(UserSubscription subscription, LocalDateTime now) {
        if (subscription.getStartDate() == null) {
            return new RefundResult(0L, RefundStatus.NOT_ELIGIBLE, 0, BigDecimal.ZERO,
                    "Không đủ điều kiện hoàn tiền vì không có ngày bắt đầu gói");
        }

        if (subscription.getSubscriptionPlan() == null
                || subscription.getSubscriptionPlan().getPrice() == null) {
            return new RefundResult(0L, RefundStatus.NOT_ELIGIBLE, 0, BigDecimal.ZERO,
                    "Không đủ điều kiện hoàn tiền vì không có thông tin giá gói");
        }

        BigDecimal price = subscription.getSubscriptionPlan().getPrice();
        long usedDays = Duration.between(subscription.getStartDate(), now).toDays() + 1;

        if (usedDays <= 5) {
            return new RefundResult(usedDays, RefundStatus.PENDING, 100, price,
                    "Huỷ trong ngày thứ 1-5: được hoàn 100% giá trị gói. Yêu cầu hoàn tiền đang chờ xử lý.");
        }

        if (usedDays <= 10) {
            BigDecimal refundAmount = price.multiply(BigDecimal.valueOf(0.5)).setScale(0, RoundingMode.HALF_UP);
            return new RefundResult(usedDays, RefundStatus.PENDING, 50, refundAmount,
                    "Huỷ trong ngày thứ 6-10: được hoàn 50% giá trị gói. Yêu cầu hoàn tiền đang chờ xử lý.");
        }

        return new RefundResult(usedDays, RefundStatus.NOT_ELIGIBLE, 0, BigDecimal.ZERO,
                "Huỷ sau ngày thứ 10: không đủ điều kiện hoàn tiền");
    }

    private record RefundResult(
            Long usedDays,
            RefundStatus refundStatus,
            Integer refundPercent,
            BigDecimal refundAmount,
            String message
    ) {
    }

    // =========================================================================
    // PRIVATE - PAYMENT HELPERS
    // =========================================================================

    private boolean isPaidPlan(SubscriptionPlan plan) {
        return plan.getPrice() != null && plan.getPrice().compareTo(BigDecimal.ZERO) > 0;
    }

    private PaymentTransaction.PaymentMethod resolvePaymentMethod(PaymentTransaction.PaymentMethod requested) {
        if (requested == null) {
            throw new ApiException(ErrorCode.INVALID_SUBSCRIPTION_REQUEST, "Vui lòng chọn phương thức thanh toán");
        }
        return requested;
    }

    private PaymentTransaction.PaymentMethod resolveRenewPaymentMethod(UserSubscription subscription) {
        PaymentTransaction lastTransaction = subscription.getTransaction();
        if (lastTransaction != null && lastTransaction.getPaymentMethod() != null) {
            return lastTransaction.getPaymentMethod();
        }
        return PaymentTransaction.PaymentMethod.VNPAY;
    }

    private SubscribePlanResponse initiatePaidSubscription(
            String username,
            SubscriptionPlan plan,
            PaymentTransaction.PaymentMethod method
    ) {
        PaymentResponseDto paymentResponse = paymentService.initPayment(username, plan.getPlanId(), method);

        return SubscribePlanResponse.builder()
                .requiresPayment(true)
                .paymentUrl(paymentResponse.getPayUrl())
                .subscription(null)
                .build();
    }

    // =========================================================================
    // PRIVATE - SUBSCRIPTION HELPERS (gói free)
    // =========================================================================

    private UserSubscriptionResponse processFreeSubscription(
            User user, SubscriptionPlan plan, SubscribePlanRequest request
    ) {
        Optional<UserSubscription> activeSubscription =
                userSubscriptionRepository.findFirstByUserAndStatusOrderByEndDateDesc(user, SubscriptionStatus.ACTIVE);

        if (activeSubscription.isPresent()) {
            return extendExistingSubscription(activeSubscription.get(), plan, request);
        }

        return createNewSubscription(user, plan, request);
    }

    private UserSubscriptionResponse extendExistingSubscription(
            UserSubscription current, SubscriptionPlan plan, SubscribePlanRequest request
    ) {
        boolean samePlan = current.getSubscriptionPlan().getPlanId().equals(plan.getPlanId());

        if (!samePlan) {
            throw new ApiException(ErrorCode.INVALID_SUBSCRIPTION_REQUEST, "Bạn đang sử dụng gói khác.");
        }

        current.setEndDate(calculateRenewEndDate(current.getEndDate(), plan.getDurationDays()));
        current.setAutoRenew(Boolean.TRUE.equals(request.getAutoRenew()));

        return userSubscriptionMapper.toResponse(userSubscriptionRepository.save(current));
    }

    private UserSubscriptionResponse createNewSubscription(
            User user, SubscriptionPlan plan, SubscribePlanRequest request
    ) {
        LocalDateTime now = LocalDateTime.now();

        UserSubscription subscription = UserSubscription.builder()
                .user(user)
                .subscriptionPlan(plan)
                .startDate(now)
                .endDate(now.plusDays(plan.getDurationDays()))
                .status(SubscriptionStatus.ACTIVE)
                .autoRenew(Boolean.TRUE.equals(request.getAutoRenew()))
                .build();

        return userSubscriptionMapper.toResponse(userSubscriptionRepository.save(subscription));
    }

    // =========================================================================
    // PRIVATE - VALIDATION
    // =========================================================================

    private void validateCreateRequest(CreateSubscriptionPlanRequest request) {
        if (request == null) {
            throw new ApiException(ErrorCode.INVALID_SUBSCRIPTION_REQUEST, "Request không được để trống");
        }
        if (request.getPlanType() == null) {
            throw new ApiException(ErrorCode.INVALID_SUBSCRIPTION_REQUEST, "Plan type không được để trống");
        }
        if (request.getPlanName() == null || request.getPlanName().trim().isEmpty()) {
            throw new ApiException(ErrorCode.INVALID_SUBSCRIPTION_REQUEST, "Tên gói không được để trống");
        }
        if (request.getPrice() != null && request.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new ApiException(ErrorCode.INVALID_SUBSCRIPTION_REQUEST, "Giá tiền phải lớn hơn hoặc bằng 0");
        }
        if (request.getDurationDays() != null && request.getDurationDays() <= 0) {
            throw new ApiException(ErrorCode.INVALID_SUBSCRIPTION_REQUEST, "Duration phải lớn hơn 0");
        }
        if (subscriptionPlanRepository.existsByPlanNameIgnoreCaseAndDeletedFalse(request.getPlanName().trim())) {
            throw new ApiException(ErrorCode.SUBSCRIPTION_PLAN_EXISTED, "Tên gói đã tồn tại");
        }
    }

    private void validatePlanForSubscription(SubscriptionPlan plan) {
        if (Boolean.TRUE.equals(plan.getDeleted())) {
            throw new ApiException(ErrorCode.SUBSCRIPTION_PLAN_NOT_FOUND, "Gói đăng ký đã bị xóa");
        }
        if (Boolean.FALSE.equals(plan.getActive())) {
            throw new ApiException(ErrorCode.SUBSCRIPTION_PLAN_INACTIVE, "Gói đăng ký chưa được kích hoạt");
        }
        if (plan.getDurationDays() == null || plan.getDurationDays() <= 0) {
            throw new ApiException(ErrorCode.INVALID_SUBSCRIPTION_REQUEST, "Duration không hợp lệ");
        }
    }

    // =========================================================================
    // PRIVATE - DEFAULTS
    // =========================================================================

    private void applyDefaults(SubscriptionPlan plan, CreateSubscriptionPlanRequest request) {
        if (plan.getPrice() == null) plan.setPrice(BigDecimal.ZERO);
        if (plan.getDurationDays() == null) plan.setDurationDays(30);
        if (plan.getMaxMembers() == null) plan.setMaxMembers(1);
        if (plan.getActive() == null) plan.setActive(request.getActive() == null || request.getActive());
        plan.setDeleted(false);
    }

    // =========================================================================
    // PRIVATE - REPOSITORY SHORTCUTS
    // =========================================================================

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND, "Không tìm thấy người dùng"));
    }

    private SubscriptionPlan findPlanById(Integer planId) {
        return subscriptionPlanRepository.findByPlanIdAndDeletedFalse(planId)
                .orElseThrow(() -> new ApiException(ErrorCode.SUBSCRIPTION_PLAN_NOT_FOUND, "Không tìm thấy gói đăng ký"));
    }

    private UserSubscription findActiveSubscription(User user) {
        UserSubscription subscription = userSubscriptionRepository
                .findFirstByUserAndStatusOrderByEndDateDesc(user, SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new ApiException(ErrorCode.ACTIVE_SUBSCRIPTION_NOT_FOUND, "Không có gói đăng ký đang hoạt động"));

        if (subscription.getEndDate() != null && subscription.getEndDate().isBefore(LocalDateTime.now())) {
            subscription.setStatus(SubscriptionStatus.EXPIRED);
            userSubscriptionRepository.save(subscription);

            throw new ApiException(ErrorCode.ACTIVE_SUBSCRIPTION_NOT_FOUND, "Gói đăng ký đã hết hạn");
        }

        return subscription;
    }

    // =========================================================================
    // PRIVATE - UTILITIES
    // =========================================================================

    private Pageable buildPageable(Integer pageNum, Integer pageSize) {
        pageNum = (pageNum == null || pageNum < 1) ? 1 : pageNum;
        pageSize = (pageSize == null || pageSize < 1) ? 10 : pageSize;
        pageSize = Math.min(pageSize, 100);

        return PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.ASC, "price").and(Sort.by("planId")));
    }

    private LocalDateTime calculateRenewEndDate(LocalDateTime currentEndDate, Integer durationDays) {
        LocalDateTime base = (currentEndDate != null && currentEndDate.isAfter(LocalDateTime.now()))
                ? currentEndDate
                : LocalDateTime.now();
        return base.plusDays(durationDays);
    }
}