package com.examp.genifit.dto.response;

import com.examp.genifit.entity.PlanType;
import com.examp.genifit.entity.SubscriptionPlan;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@JsonPropertyOrder({
        "planId",
        "planType",
        "planName",
        "description",
        "price",
        "durationDays",
        "aiScanLimitPerMonth",
        "mealSuggestionLimitPerMonth",
        "reminderLimit",
        "maxMembers",
        "maxClients",
        "trial",
        "familySharingEnabled",
        "coachFeaturesEnabled",
        "mealPlanEnabled",
        "weeklyReportEnabled",
        "monthlyReportEnabled",
        "exportReportEnabled",
        "macroTrackingEnabled",
        "calorieDeficitTrackingEnabled",
        "calorieSurplusTrackingEnabled",
        "bloodSugarControlEnabled",
        "active"
})
public class SubscriptionPlanResponse {

    private Integer planId;
    private PlanType planType;
    private String planName;
    private String description;
    private BigDecimal price;
    private Integer durationDays;

    private Integer aiScanLimitPerMonth;
    private Integer mealSuggestionLimitPerMonth;
    private Integer reminderLimit;

    private Integer maxMembers;
    private Integer maxClients;

    private Boolean trial;
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

    private Boolean active;

    public SubscriptionPlanResponse(SubscriptionPlan plan) {
        this.planId = plan.getPlanId();
        this.planType = plan.getPlanType();
        this.planName = plan.getPlanName();
        this.description = plan.getDescription();
        this.price = plan.getPrice();
        this.durationDays = plan.getDurationDays();
        this.aiScanLimitPerMonth = plan.getAiScanLimitPerMonth();
        this.mealSuggestionLimitPerMonth = plan.getMealSuggestionLimitPerMonth();
        this.reminderLimit = plan.getReminderLimit();
        this.maxMembers = plan.getMaxMembers();
        this.maxClients = plan.getMaxClients();
        this.trial = plan.getTrial();
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
        this.active = plan.getActive();
    }
}