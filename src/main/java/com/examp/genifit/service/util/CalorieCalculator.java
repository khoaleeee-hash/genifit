package com.examp.genifit.service.util;

import com.examp.genifit.entity.ActivityLevel;
import com.examp.genifit.entity.Gender;
import com.examp.genifit.entity.GoalType;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CalorieCalculator {

    public double calculateDailyCalories(
            Gender gender,
            int age,
            double heightCm,
            double weightKg,
            ActivityLevel activityLevel,
            GoalType goal
    ) {

        double bmr;

        if (gender == Gender.MALE) {
            bmr = 10 * weightKg + 6.25 * heightCm - 5 * age + 5;
        } else {
            bmr = 10 * weightKg + 6.25 * heightCm - 5 * age - 161;
        }

        double activityFactor = switch (activityLevel) {
            case SEDENTARY -> 1.2;
            case LIGHTLY_ACTIVE -> 1.375;
            case MODERATELY_ACTIVE -> 1.55;
            case VERY_ACTIVE -> 1.725;
        };

        double tdee = bmr * activityFactor;

        return switch (goal) {
            case LOSE_WEIGHT -> tdee - 500;
            case GAIN_WEIGHT -> tdee + 500;
            case MAINTAIN -> tdee;
        };
    }

}