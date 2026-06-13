package com.examp.genifit.service.serviceImpl;

import com.examp.genifit.dto.request.CreateSubscriptionPlanRequest;
import com.examp.genifit.dto.request.UpdateSubscriptionPlanRequest;
import com.examp.genifit.dto.response.SubscriptionPlanResponse;
import com.examp.genifit.entity.SubscriptionPlan;
import com.examp.genifit.repository.SubscriptionPlanRepository;
import com.examp.genifit.service.SubscriptionPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionPlanServiceImpl implements SubscriptionPlanService {

    private final SubscriptionPlanRepository subscriptionPlanRepository;

    @Override
    public List<SubscriptionPlanResponse> getActivePlans() {
        return subscriptionPlanRepository.findByActiveTrueOrderByPriceAsc()
                .stream()
                .map(SubscriptionPlanResponse::new)
                .toList();
    }

    @Override
    public SubscriptionPlanResponse getPlanById(Integer planId) {
        SubscriptionPlan plan = subscriptionPlanRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy gói đăng ký"));

        return new SubscriptionPlanResponse(plan);
    }

    @Override
    public SubscriptionPlanResponse createPlan(CreateSubscriptionPlanRequest request) {
        validateCreateRequest(request);

        if (subscriptionPlanRepository.existsByPlanType(request.getPlanType())) {
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
                .build();

        SubscriptionPlan savedPlan = subscriptionPlanRepository.save(plan);

        return new SubscriptionPlanResponse(savedPlan);
    }

    @Override
    public SubscriptionPlanResponse updatePlan(Integer planId, UpdateSubscriptionPlanRequest request) {
        SubscriptionPlan plan = subscriptionPlanRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy gói đăng ký"));

        if (request.getPlanType() != null && !request.getPlanType().equals(plan.getPlanType())) {
            if (subscriptionPlanRepository.existsByPlanType(request.getPlanType())) {
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
        SubscriptionPlan plan = subscriptionPlanRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy gói đăng ký"));

        // Xoá mềm, không xoá khỏi database
        plan.setActive(false);

        subscriptionPlanRepository.save(plan);
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
}