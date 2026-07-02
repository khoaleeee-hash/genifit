package com.examp.genifit.util;

import com.examp.genifit.entity.ActivityLevel;
import com.examp.genifit.entity.Gender;
import com.examp.genifit.entity.GoalType;

public class CalorieCalculatorUtil {

    public static Double calculateTargetCalorie(Double weight, Double height, Integer age, Gender gender, ActivityLevel activityLevel, GoalType goal) {
        if (weight == null || height == null || age == null || gender == null || activityLevel == null || goal == null) {
            return 0.0;
        }
        
        double bmr = (10 * weight) + (6.25 * height) - (5 * age);
        bmr = (gender == Gender.MALE) ? (bmr + 5) : (bmr - 161);

        double tdee;
        switch (activityLevel) {
            case LIGHTLY_ACTIVE:
                tdee = bmr * 1.375;
                break;
            case MODERATELY_ACTIVE:
                tdee = bmr * 1.55;
                break;
            case VERY_ACTIVE:
                tdee = bmr * 1.725;
                break;
            case SEDENTARY:
            default:
                tdee = bmr * 1.2;
                break;
        }

        double targetCalorie = tdee;
        if (goal == GoalType.LOSE_WEIGHT) {
            targetCalorie -= 500;
        } else if (goal == GoalType.GAIN_WEIGHT) {
            targetCalorie += 500;
        }

        double minCalorie = (gender == Gender.MALE) ? 1500.0 : 1200.0;

        return Math.max(targetCalorie, minCalorie);
    }
}