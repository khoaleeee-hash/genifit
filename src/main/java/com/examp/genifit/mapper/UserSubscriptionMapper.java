package com.examp.genifit.mapper;

import com.examp.genifit.dto.response.MySubscriptionResponse;
import com.examp.genifit.dto.response.UserSubscriptionResponse;
import com.examp.genifit.entity.SubscriptionPlan;
import com.examp.genifit.entity.UserSubscription;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface UserSubscriptionMapper {

    @Mapping(target = "userId", source = "user.userId")
    @Mapping(target = "username", source = "user.username")

    @Mapping(target = "planId", source = "subscriptionPlan.planId")
    @Mapping(target = "planType", source = "subscriptionPlan.planType")
    @Mapping(target = "planName", source = "subscriptionPlan.planName")

    UserSubscriptionResponse toResponse(UserSubscription entity);


    @Mapping(target = "subscriptionId", source = "subscriptionId")

    @Mapping(target = "planId", source = "subscriptionPlan.planId")
    @Mapping(target = "planType", expression = "java(entity.getSubscriptionPlan().getPlanType().name())")
    @Mapping(target = "planName", source = "subscriptionPlan.planName")
    @Mapping(target = "description", source = "subscriptionPlan.description")

    @Mapping(target = "active", source = "subscriptionPlan.active")

    @Mapping(target = "status", expression = "java(entity.getStatus().name())")

    @Mapping(target = "aiScanLimitPerMonth", source = "subscriptionPlan.aiScanLimitPerMonth")
    @Mapping(target = "mealSuggestionLimitPerMonth", source = "subscriptionPlan.mealSuggestionLimitPerMonth")
    @Mapping(target = "reminderLimit", source = "subscriptionPlan.reminderLimit")

    @Mapping(target = "familySharingEnabled", source = "subscriptionPlan.familySharingEnabled")
    @Mapping(target = "coachFeaturesEnabled", source = "subscriptionPlan.coachFeaturesEnabled")
    @Mapping(target = "mealPlanEnabled", source = "subscriptionPlan.mealPlanEnabled")

    @Mapping(target = "weeklyReportEnabled", source = "subscriptionPlan.weeklyReportEnabled")
    @Mapping(target = "monthlyReportEnabled", source = "subscriptionPlan.monthlyReportEnabled")
    @Mapping(target = "exportReportEnabled", source = "subscriptionPlan.exportReportEnabled")

    @Mapping(target = "macroTrackingEnabled", source = "subscriptionPlan.macroTrackingEnabled")
    @Mapping(target = "calorieDeficitTrackingEnabled", source = "subscriptionPlan.calorieDeficitTrackingEnabled")
    @Mapping(target = "calorieSurplusTrackingEnabled", source = "subscriptionPlan.calorieSurplusTrackingEnabled")
    @Mapping(target = "bloodSugarControlEnabled", source = "subscriptionPlan.bloodSugarControlEnabled")

    MySubscriptionResponse toMySubscription(UserSubscription entity);

}