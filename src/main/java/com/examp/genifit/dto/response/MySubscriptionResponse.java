package com.examp.genifit.dto.response;

import com.examp.genifit.entity.UserSubscription;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MySubscriptionResponse {

    private Integer subscriptionId;

    private Integer planId;

    private String planType;

    private String planName;

    private String description;

    private Boolean active;

    private String status;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private Integer aiScanLimitPerMonth;

    private Integer mealSuggestionLimitPerMonth;

    private Integer reminderLimit;

    private Boolean familySharingEnabled;

    private Boolean coachFeaturesEnabled;

    private Boolean mealPlanEnabled;

    private Boolean weeklyReportEnabled;

    private Boolean monthlyReportEnabled;

    private Boolean exportReportEnabled;

    private Boolean macroTrackingEnabled;

    private Boolean calorieDeficitTrackingEnabled;

    private Boolean calorieSurplusTrackingEnabled;

    private Boolean bloodSugarControlEnabled;

    public MySubscriptionResponse(UserSubscription userSubscription) {
        this.subscriptionId = userSubscription.getSubscriptionId();

        this.status = userSubscription.getStatus().name();
        this.startDate = userSubscription.getStartDate();
        this.endDate = userSubscription.getEndDate();

        if (userSubscription.getSubscriptionPlan() != null) {
            var plan = userSubscription.getSubscriptionPlan();

            this.planId = plan.getPlanId();
            this.planType = plan.getPlanType().name();
            this.planName = plan.getPlanName();
            this.description = plan.getDescription();
            this.active = plan.getActive();

            this.aiScanLimitPerMonth = plan.getAiScanLimitPerMonth();
            this.mealSuggestionLimitPerMonth = plan.getMealSuggestionLimitPerMonth();
            this.reminderLimit = plan.getReminderLimit();

            this.familySharingEnabled = plan.getFamilySharingEnabled();
            this.coachFeaturesEnabled = plan.getCoachFeaturesEnabled();
            this.mealPlanEnabled = plan.getMealPlanEnabled();
            this.weeklyReportEnabled = plan.getWeeklyReportEnabled();
            this.monthlyReportEnabled = plan.getMonthlyReportEnabled();
            this.exportReportEnabled = plan.getExportReportEnabled();

            this.macroTrackingEnabled = plan.getMacroTrackingEnabled();
            this.calorieDeficitTrackingEnabled = plan.getCalorieDeficitTrackingEnabled();
            this.calorieSurplusTrackingEnabled = plan.getCalorieSurplusTrackingEnabled();
            this.bloodSugarControlEnabled = plan.getBloodSugarControlEnabled();
        }
    }
}