package com.examp.genifit.dto.request;

import com.examp.genifit.entity.ActivityLevel;
import com.examp.genifit.entity.Gender;
import com.examp.genifit.entity.GoalType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateUserProfileRequest {
    String firstName;

    String lastName;

    @Past(message = "Ngày sinh phải là một ngày trong quá khứ")
    LocalDate dateOfBirth;

    String occupation;

    @NotNull(message = "Chiều cao không được để trống")
    Double heightCm;

    @NotNull(message = "Cân nặng không được để trống")
    Double weightKg;

    @NotNull(message = "Tuổi không được để trống")
    Integer age;

    @NotNull(message = "Giới tính không được để trống")
    Gender gender;

    @NotNull(message = "Mục tiêu không được để trống")
    GoalType goal;

    @NotNull(message = "Mức độ vận động không được để trống")
    ActivityLevel activityLevel;

    Double targetWeightKg;
}