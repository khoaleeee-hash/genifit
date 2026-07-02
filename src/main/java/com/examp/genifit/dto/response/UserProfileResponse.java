package com.examp.genifit.dto.response;

import com.examp.genifit.entity.ActivityLevel;
import com.examp.genifit.entity.Gender;
import com.examp.genifit.entity.GoalType;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserProfileResponse {
    Double heightCm;
    Double weightKg;
    Integer age;
    Gender gender;
    GoalType goal;
    Double baseTargetCalorie;
    ActivityLevel activityLevel;
    Double targetWeightKg;
}