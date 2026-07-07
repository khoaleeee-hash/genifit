package com.examp.genifit.dto.response;

import com.examp.genifit.entity.ActivityLevel;
import com.examp.genifit.entity.Gender;
import com.examp.genifit.entity.GoalType;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserProfileResponse {
    String firstName;
    String lastName;
    LocalDate dateOfBirth;
    String occupation;
    Double heightCm;
    Double weightKg;
    Integer age;
    Gender gender;
    GoalType goal;
    ActivityLevel activityLevel;
    Double targetWeightKg;
    Double baseTargetCalorie;
}