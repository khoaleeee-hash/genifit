package com.examp.genifit.service.serviceImpl;

import com.examp.genifit.common.exception.ApiException;
import com.examp.genifit.common.exception.ErrorCode;
import com.examp.genifit.dto.request.CreateSubscriptionPlanRequest;
import com.examp.genifit.dto.request.SubscribePlanRequest;
import com.examp.genifit.dto.request.UpdateSubscriptionPlanRequest;
import com.examp.genifit.dto.response.MySubscriptionResponse;
import com.examp.genifit.dto.response.SubscriptionPlanResponse;
import com.examp.genifit.dto.response.UserSubscriptionResponse;
import com.examp.genifit.entity.SubscriptionPlan;
import com.examp.genifit.entity.SubscriptionStatus;
import com.examp.genifit.entity.User;
import com.examp.genifit.entity.UserSubscription;
import com.examp.genifit.mapper.SubscriptionPlanMapper;
import com.examp.genifit.mapper.UserSubscriptionMapper;
import com.examp.genifit.repository.SubscriptionPlanRepository;
import com.examp.genifit.repository.UserRepository;
import com.examp.genifit.repository.UserSubscriptionRepository;
import com.examp.genifit.service.SubscriptionPlanService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    public UserSubscriptionResponse subscribePlan(String username, SubscribePlanRequest request) {
        User user = findUserByUsername(username);
        SubscriptionPlan plan = findPlanById(request.getPlanId());

        validatePlanForSubscription(plan);
        requireFreePlan(plan);

        Optional<UserSubscription> activeSubscription =
                userSubscriptionRepository.findFirstByUserAndStatusOrderByEndDateDesc(
                        user,
                        SubscriptionStatus.ACTIVE
                );

        if (activeSubscription.isPresent()) {
            return extendExistingSubscription(activeSubscription.get(), plan, request);
        }

        return createNewSubscription(user, plan, request);
    }

    @Override
    public UserSubscriptionResponse cancelSubscription(String username) {
        User user = findUserByUsername(username);
        UserSubscription subscription = findActiveSubscription(user);

        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscription.setCancelledAt(LocalDateTime.now());
        subscription.setAutoRenew(false);

        return userSubscriptionMapper.toResponse(userSubscriptionRepository.save(subscription));
    }

    @Override
    public UserSubscriptionResponse renewSubscription(String username) {
        User user = findUserByUsername(username);
        UserSubscription subscription = findActiveSubscription(user);
        SubscriptionPlan plan = subscription.getSubscriptionPlan();

        validatePlanForSubscription(plan);
        requireFreePlan(plan);

        subscription.setEndDate(
                calculateRenewEndDate(subscription.getEndDate(), plan.getDurationDays())
        );

        return userSubscriptionMapper.toResponse(userSubscriptionRepository.save(subscription));
    }

    // =========================================================================
    // PRIVATE - SUBSCRIPTION HELPERS
    // =========================================================================

    private UserSubscriptionResponse extendExistingSubscription(
            UserSubscription current,
            SubscriptionPlan plan,
            SubscribePlanRequest request
    ) {
        boolean samePlan = current.getSubscriptionPlan().getPlanId().equals(plan.getPlanId());

        if (!samePlan) {
            throw new ApiException(
                    ErrorCode.INVALID_SUBSCRIPTION_REQUEST,
                    "Bạn đang sử dụng gói khác."
            );
        }

        current.setEndDate(calculateRenewEndDate(current.getEndDate(), plan.getDurationDays()));
        current.setAutoRenew(Boolean.TRUE.equals(request.getAutoRenew()));

        return userSubscriptionMapper.toResponse(userSubscriptionRepository.save(current));
    }

    private UserSubscriptionResponse createNewSubscription(
            User user,
            SubscriptionPlan plan,
            SubscribePlanRequest request
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
            throw new ApiException(
                    ErrorCode.INVALID_SUBSCRIPTION_REQUEST,
                    "Request không được để trống"
            );
        }
        if (request.getPlanType() == null) {
            throw new ApiException(
                    ErrorCode.INVALID_SUBSCRIPTION_REQUEST,
                    "Plan type không được để trống"
            );
        }
        if (request.getPlanName() == null || request.getPlanName().trim().isEmpty()) {
            throw new ApiException(
                    ErrorCode.INVALID_SUBSCRIPTION_REQUEST,
                    "Tên gói không được để trống"
            );
        }
        if (request.getPrice() != null && request.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new ApiException(
                    ErrorCode.INVALID_SUBSCRIPTION_REQUEST,
                    "Giá tiền phải lớn hơn hoặc bằng 0"
            );
        }
        if (request.getDurationDays() != null && request.getDurationDays() <= 0) {
            throw new ApiException(
                    ErrorCode.INVALID_SUBSCRIPTION_REQUEST,
                    "Duration phải lớn hơn 0"
            );
        }
        if (subscriptionPlanRepository.existsByPlanNameIgnoreCaseAndDeletedFalse(
                request.getPlanName().trim())) {
            throw new ApiException(
                    ErrorCode.SUBSCRIPTION_PLAN_EXISTED,
                    "Tên gói đã tồn tại"
            );
        }
    }

    private void validatePlanForSubscription(SubscriptionPlan plan) {
        if (Boolean.TRUE.equals(plan.getDeleted())) {
            throw new ApiException(
                    ErrorCode.SUBSCRIPTION_PLAN_NOT_FOUND,
                    "Gói đăng ký đã bị xóa"
            );
        }
        if (Boolean.FALSE.equals(plan.getActive())) {
            throw new ApiException(
                    ErrorCode.SUBSCRIPTION_PLAN_INACTIVE,
                    "Gói đăng ký chưa được kích hoạt"
            );
        }
        if (plan.getDurationDays() == null || plan.getDurationDays() <= 0) {
            throw new ApiException(
                    ErrorCode.INVALID_SUBSCRIPTION_REQUEST,
                    "Duration không hợp lệ"
            );
        }
    }

    /**
     * Chặn gói trả phí.
     * Sau này thay bằng: paymentService.createPayment(...)
     */
    private void requireFreePlan(SubscriptionPlan plan) {
        if (plan.getPrice().compareTo(BigDecimal.ZERO) > 0) {
            throw new ApiException(
                    ErrorCode.PAYMENT_REQUIRED,
                    "Gói trả phí cần thanh toán trước."
            );
        }
    }

    // =========================================================================
    // PRIVATE - DEFAULTS
    // =========================================================================

    /**
     * Áp dụng default value sau khi MapStruct đã map từ request.
     */
    private void applyDefaults(SubscriptionPlan plan, CreateSubscriptionPlanRequest request) {
        if (plan.getPrice() == null)        plan.setPrice(BigDecimal.ZERO);
        if (plan.getDurationDays() == null) plan.setDurationDays(30);
        if (plan.getMaxMembers() == null)   plan.setMaxMembers(1);
        if (plan.getActive() == null)       plan.setActive(request.getActive() == null || request.getActive());
        plan.setDeleted(false);
    }

    // =========================================================================
    // PRIVATE - REPOSITORY SHORTCUTS
    // =========================================================================

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException(
                        ErrorCode.USER_NOT_FOUND,
                        "Không tìm thấy người dùng"
                ));
    }

    private SubscriptionPlan findPlanById(Integer planId) {
        return subscriptionPlanRepository.findByPlanIdAndDeletedFalse(planId)
                .orElseThrow(() -> new ApiException(
                        ErrorCode.SUBSCRIPTION_PLAN_NOT_FOUND,
                        "Không tìm thấy gói đăng ký"
                ));
    }

    private UserSubscription findActiveSubscription(User user) {
        return userSubscriptionRepository
                .findFirstByUserAndStatusOrderByEndDateDesc(user, SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new ApiException(
                        ErrorCode.ACTIVE_SUBSCRIPTION_NOT_FOUND,
                        "Không có gói đăng ký đang hoạt động"
                ));
    }

    // =========================================================================
    // PRIVATE - UTILITIES
    // =========================================================================

    private Pageable buildPageable(Integer pageNum, Integer pageSize) {
        pageNum  = (pageNum  == null || pageNum  < 1) ? 1  : pageNum;
        pageSize = (pageSize == null || pageSize < 1) ? 10 : pageSize;
        pageSize = Math.min(pageSize, 100);

        return PageRequest.of(
                pageNum - 1,
                pageSize,
                Sort.by(Sort.Direction.ASC, "price").and(Sort.by("planId"))
        );
    }

    private LocalDateTime calculateRenewEndDate(LocalDateTime currentEndDate, Integer durationDays) {
        LocalDateTime base = (currentEndDate != null && currentEndDate.isAfter(LocalDateTime.now()))
                ? currentEndDate
                : LocalDateTime.now();
        return base.plusDays(durationDays);
    }
}