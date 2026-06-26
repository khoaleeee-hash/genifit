package com.examp.genifit.service.serviceImpl;

import com.examp.genifit.dto.request.CreateSubscriptionPlanRequest;
import com.examp.genifit.dto.request.SubscribePlanRequest;
import com.examp.genifit.dto.request.UpdateSubscriptionPlanRequest;
import com.examp.genifit.dto.response.*;
import com.examp.genifit.entity.SubscriptionPlan;
import com.examp.genifit.entity.SubscriptionStatus;
import com.examp.genifit.entity.User;
import com.examp.genifit.entity.UserSubscription;
import com.examp.genifit.repository.SubscriptionPlanRepository;
import com.examp.genifit.repository.UserRepository;
import com.examp.genifit.repository.UserSubscriptionRepository;
import com.examp.genifit.service.SubscriptionPlanService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SubscriptionPlanServiceImpl implements SubscriptionPlanService {

    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final UserRepository userRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;

    @Override
    public PageResponse<SubscriptionPlanResponse> getAllPlans(Integer pageNum, Integer pageSize) {
        Pageable pageable = buildPageable(pageNum, pageSize);

        Page<SubscriptionPlanResponse> page = subscriptionPlanRepository.findByDeletedFalse(pageable)
                .map(SubscriptionPlanResponse::new);

        return buildPageResponse(page);
    }

    @Override
    public PageResponse<SubscriptionPlanResponse> getActivePlans(Integer pageNum, Integer pageSize) {
        Pageable pageable = buildPageable(pageNum, pageSize);

        Page<SubscriptionPlanResponse> page = subscriptionPlanRepository.findByActiveTrueAndDeletedFalse(pageable)
                .map(SubscriptionPlanResponse::new);

        return buildPageResponse(page);
    }

    @Override
    public SubscriptionPlanResponse getPlanById(Integer planId) {
        SubscriptionPlan plan = subscriptionPlanRepository.findByPlanIdAndDeletedFalse(planId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy gói đăng ký"));

        return new SubscriptionPlanResponse(plan);
    }

    @Override
    public MySubscriptionResponse getMySubscription(String username) {
        User user = userRepository.findByUsernameAndIsActiveTrue(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        UserSubscription userSubscription = userSubscriptionRepository
                .findByUserUserIdAndStatus(user.getUserId(), SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new RuntimeException("Người dùng chưa có gói đăng ký đang hoạt động"));

        return new MySubscriptionResponse(userSubscription);
    }

    @Override
    public SubscriptionPlanResponse createPlan(CreateSubscriptionPlanRequest request) {
        validateCreateRequest(request);

        if (subscriptionPlanRepository.existsByPlanTypeAndDeletedFalse(request.getPlanType())) {
            throw new RuntimeException("Plan type này đã tồn tại");
        }

        SubscriptionPlan plan = SubscriptionPlan.builder()
                .planType(request.getPlanType())
                .planName(request.getPlanName())
                .description(request.getDescription())
                .price(request.getPrice() == null ? BigDecimal.ZERO : request.getPrice())
                .durationDays(request.getDurationDays() == null ? 30 : request.getDurationDays())

                .aiScanLimitPerMonth(request.getAiScanLimitPerMonth() == null ? 0 : request.getAiScanLimitPerMonth())
                .mealSuggestionLimitPerMonth(request.getMealSuggestionLimitPerMonth() == null ? 0 : request.getMealSuggestionLimitPerMonth())
                .reminderLimit(request.getReminderLimit() == null ? 0 : request.getReminderLimit())

                .maxMembers(request.getMaxMembers() == null ? 1 : request.getMaxMembers())
                .maxClients(request.getMaxClients() == null ? 0 : request.getMaxClients())

                .trial(request.getTrial() == null ? false : request.getTrial())
                .familySharingEnabled(request.getFamilySharingEnabled() == null ? false : request.getFamilySharingEnabled())
                .coachFeaturesEnabled(request.getCoachFeaturesEnabled() == null ? false : request.getCoachFeaturesEnabled())

                .mealPlanEnabled(request.getMealPlanEnabled() == null ? false : request.getMealPlanEnabled())
                .weeklyReportEnabled(request.getWeeklyReportEnabled() == null ? false : request.getWeeklyReportEnabled())
                .monthlyReportEnabled(request.getMonthlyReportEnabled() == null ? false : request.getMonthlyReportEnabled())
                .exportReportEnabled(request.getExportReportEnabled() == null ? false : request.getExportReportEnabled())

                .macroTrackingEnabled(request.getMacroTrackingEnabled() == null ? false : request.getMacroTrackingEnabled())
                .calorieDeficitTrackingEnabled(request.getCalorieDeficitTrackingEnabled() == null ? false : request.getCalorieDeficitTrackingEnabled())
                .calorieSurplusTrackingEnabled(request.getCalorieSurplusTrackingEnabled() == null ? false : request.getCalorieSurplusTrackingEnabled())
                .bloodSugarControlEnabled(request.getBloodSugarControlEnabled() == null ? false : request.getBloodSugarControlEnabled())
                .active(request.getActive() == null ? true : request.getActive())
                .deleted(false)
                .build();

        SubscriptionPlan savedPlan = subscriptionPlanRepository.save(plan);

        return new SubscriptionPlanResponse(savedPlan);
    }

    @Override
    @Transactional
    public UserSubscriptionResponse subscribePlan(
            String username,
            SubscribePlanRequest request
    ) {
        User user = userRepository.findByUsernameAndIsActiveTrue(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user đang đăng nhập"));

        SubscriptionPlan plan = subscriptionPlanRepository.findById(request.getPlanId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy gói đăng kí"));

        if (Boolean.FALSE.equals(plan.getActive())) {
            throw new RuntimeException("Gói đăng kí này hiện không còn hoạt động");
        }

        if (Boolean.TRUE.equals(plan.getDeleted())) {
            throw new RuntimeException("Gói đăng kí này đã bị xoá");
        }

        if (plan.getDurationDays() == null || plan.getDurationDays() <= 0) {
            throw new RuntimeException("Thời hạn gói đăng kí không hợp lệ");
        }

        userSubscriptionRepository
                .findFirstByUserAndStatusOrderByEndDateDesc(user, SubscriptionStatus.ACTIVE)
                .ifPresent(currentSubscription -> {
                    currentSubscription.setStatus(SubscriptionStatus.CANCELLED);
                    currentSubscription.setCancelledAt(LocalDateTime.now());
                    currentSubscription.setAutoRenew(false);
                    userSubscriptionRepository.save(currentSubscription);
                });

        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusDays(plan.getDurationDays());

        UserSubscription newSubscription = UserSubscription.builder()
                .user(user)
                .subscriptionPlan(plan)
                .startDate(startDate)
                .endDate(endDate)
                .status(SubscriptionStatus.ACTIVE)
//                .autoRenew(Boolean.TRUE.equals(request.getAutoRenew()))
                .autoRenew(false)
                .build();

        UserSubscription saved = userSubscriptionRepository.save(newSubscription);

        return new UserSubscriptionResponse(saved);
    }

    @Override
    public SubscriptionPlanResponse updatePlan(Integer planId, UpdateSubscriptionPlanRequest request) {
        SubscriptionPlan plan = subscriptionPlanRepository.findByPlanIdAndDeletedFalse(planId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy gói đăng ký"));

        if (request.getPlanType() != null && !request.getPlanType().equals(plan.getPlanType())) {
            if (subscriptionPlanRepository.existsByPlanTypeAndDeletedFalse(request.getPlanType())) {
                throw new RuntimeException("Plan type này đã tồn tại");
            }
            plan.setPlanType(request.getPlanType());
        }

        if (request.getPlanName() != null) {
            plan.setPlanName(request.getPlanName());
        }

        if (request.getDescription() != null) {
            plan.setDescription(request.getDescription());
        }

        if (request.getPrice() != null) {
            plan.setPrice(request.getPrice());
        }

        if (request.getDurationDays() != null) {
            plan.setDurationDays(request.getDurationDays());
        }

        if (request.getAiScanLimitPerMonth() != null) {
            plan.setAiScanLimitPerMonth(request.getAiScanLimitPerMonth());
        }

        if (request.getMealSuggestionLimitPerMonth() != null) {
            plan.setMealSuggestionLimitPerMonth(request.getMealSuggestionLimitPerMonth());
        }

        if (request.getReminderLimit() != null) {
            plan.setReminderLimit(request.getReminderLimit());
        }

        if (request.getMaxMembers() != null) {
            plan.setMaxMembers(request.getMaxMembers());
        }

        if (request.getMaxClients() != null) {
            plan.setMaxClients(request.getMaxClients());
        }

        if (request.getTrial() != null) {
            plan.setTrial(request.getTrial());
        }

        if (request.getFamilySharingEnabled() != null) {
            plan.setFamilySharingEnabled(request.getFamilySharingEnabled());
        }

        if (request.getCoachFeaturesEnabled() != null) {
            plan.setCoachFeaturesEnabled(request.getCoachFeaturesEnabled());
        }

        if (request.getMealPlanEnabled() != null) {
            plan.setMealPlanEnabled(request.getMealPlanEnabled());
        }

        if (request.getWeeklyReportEnabled() != null) {
            plan.setWeeklyReportEnabled(request.getWeeklyReportEnabled());
        }

        if (request.getMonthlyReportEnabled() != null) {
            plan.setMonthlyReportEnabled(request.getMonthlyReportEnabled());
        }

        if (request.getExportReportEnabled() != null) {
            plan.setExportReportEnabled(request.getExportReportEnabled());
        }

        if (request.getMacroTrackingEnabled() != null) {
            plan.setMacroTrackingEnabled(request.getMacroTrackingEnabled());
        }

        if (request.getCalorieDeficitTrackingEnabled() != null) {
            plan.setCalorieDeficitTrackingEnabled(request.getCalorieDeficitTrackingEnabled());
        }

        if (request.getCalorieSurplusTrackingEnabled() != null) {
            plan.setCalorieSurplusTrackingEnabled(request.getCalorieSurplusTrackingEnabled());
        }

        if (request.getBloodSugarControlEnabled() != null) {
            plan.setBloodSugarControlEnabled(request.getBloodSugarControlEnabled());
        }

        if (request.getActive() != null) {
            plan.setActive(request.getActive());
        }

        SubscriptionPlan updatedPlan = subscriptionPlanRepository.save(plan);

        return new SubscriptionPlanResponse(updatedPlan);
    }

    @Override
    public void deletePlan(Integer planId) {
        SubscriptionPlan plan = subscriptionPlanRepository.findByPlanIdAndDeletedFalse(planId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy gói đăng ký"));

        plan.setDeleted(true);
        plan.setActive(false);

        subscriptionPlanRepository.save(plan);
    }

    private Pageable buildPageable(Integer pageNum, Integer pageSize) {
        if (pageNum == null || pageNum < 0) {
            pageNum = 1;
        }

        if (pageSize == null || pageSize <= 0) {
            pageSize = 10;
        }

        if (pageSize > 50) {
            pageSize = 50;
        }

        return PageRequest.of(
                pageNum - 1,
                pageSize,
                Sort.by(Sort.Direction.ASC, "planId")
        );
    }

    private void validateCreateRequest(CreateSubscriptionPlanRequest request) {
        if (request == null) {
            throw new RuntimeException("Request không được để trống");
        }

        if (request.getPlanType() == null) {
            throw new RuntimeException("planType không được để trống");
        }

        if (request.getPlanName() == null || request.getPlanName().trim().isEmpty()) {
            throw new RuntimeException("planName không được để trống");
        }

        if (request.getPrice() != null && request.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("price không được nhỏ hơn 0");
        }

        if (request.getDurationDays() != null && request.getDurationDays() <= 0) {
            throw new RuntimeException("durationDays phải lớn hơn 0");
        }
    }

    private PageResponse<SubscriptionPlanResponse> buildPageResponse(Page<SubscriptionPlanResponse> page) {
        return PageResponse.<SubscriptionPlanResponse>builder()
                .content(page.getContent())
                .pageInfo(
                        PageInfoResponse.builder()
                                .pageNum(page.getNumber() + 1)
                                .pageSize(page.getSize())
                                .totalPage(page.getTotalPages())
                                .totalItem(page.getTotalElements())
                                .build()
                )
                .build();
    }
}