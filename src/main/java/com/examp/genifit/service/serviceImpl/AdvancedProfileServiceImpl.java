package com.examp.genifit.service.serviceImpl;

import com.examp.genifit.common.exception.ApiException;
import com.examp.genifit.common.exception.ErrorCode;
import com.examp.genifit.dto.request.CreateAdvancedProfileRequest;
import com.examp.genifit.dto.request.UpdateAdvancedProfileRequest;
import com.examp.genifit.dto.response.AdvancedProfileResponse;
import com.examp.genifit.entity.AdvancedProfile;
import com.examp.genifit.entity.User;
import com.examp.genifit.entity.UserProfile;
import com.examp.genifit.repository.AdvancedProfileRepository;
import com.examp.genifit.repository.UserProfileRepository;
import com.examp.genifit.repository.UserRepository;
import com.examp.genifit.service.AdvancedProfileService;
import com.examp.genifit.service.util.CalorieCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AdvancedProfileServiceImpl implements AdvancedProfileService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final AdvancedProfileRepository advancedProfileRepository;

    @Override
    public AdvancedProfileResponse createAdvancedProfile(
            Integer userId,
            CreateAdvancedProfileRequest request
    ) {

        if (advancedProfileRepository.existsByUser_UserId(userId)) {
            throw new ApiException(ErrorCode.ADVANCED_PROFILE_ALREADY_EXISTS);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        UserProfile userProfile = getUserProfile(userId);

        double dailyCalories = calculateDailyCalories(userProfile);

        AdvancedProfile advancedProfile = AdvancedProfile.builder()
                .user(user)
                .initialWeight(userProfile.getWeightKg())
                .targetWeight(request.getTargetWeight())
                .targetDate(request.getTargetDate())
                .dailyTargetCalorie(dailyCalories)
                .build();

        advancedProfile = advancedProfileRepository.save(advancedProfile);

        updateUserProfile(
                userProfile,
                request.getTargetWeight(),
                dailyCalories,
                request.getMedicalConditions(),
                request.getAllergies()
        );

        return mapToResponse(advancedProfile, userProfile);
    }

    @Override
    public AdvancedProfileResponse updateAdvancedProfile(
            Integer userId,
            UpdateAdvancedProfileRequest request
    ) {

        AdvancedProfile advancedProfile = advancedProfileRepository
                .findByUser_UserId(userId)
                .orElseThrow(() ->
                        new ApiException(ErrorCode.ADVANCED_PROFILE_NOT_FOUND));

        UserProfile userProfile = getUserProfile(userId);

        boolean goalChanged =
                !advancedProfile.getTargetWeight().equals(request.getTargetWeight())
                        || !advancedProfile.getTargetDate().equals(request.getTargetDate());

        /*
            90 -> 70
            đạt 70
            đổi goal 70 -> 65

            initialWeight sẽ reset thành 70
         */
        if (goalChanged) {
            advancedProfile.setInitialWeight(userProfile.getWeightKg());
        }

        double dailyCalories = calculateDailyCalories(userProfile);

        advancedProfile.setTargetWeight(request.getTargetWeight());
        advancedProfile.setTargetDate(request.getTargetDate());
        advancedProfile.setDailyTargetCalorie(dailyCalories);

        advancedProfile = advancedProfileRepository.save(advancedProfile);

        updateUserProfile(
                userProfile,
                request.getTargetWeight(),
                dailyCalories,
                request.getMedicalConditions(),
                request.getAllergies()
        );

        return mapToResponse(advancedProfile, userProfile);
    }

    @Override
    @Transactional(readOnly = true)
    public AdvancedProfileResponse getMyAdvancedProfile(Integer userId) {

        AdvancedProfile advancedProfile = advancedProfileRepository
                .findByUser_UserId(userId)
                .orElseThrow(() ->
                        new ApiException(ErrorCode.ADVANCED_PROFILE_NOT_FOUND));

        UserProfile userProfile = getUserProfile(userId);

        return mapToResponse(advancedProfile, userProfile);
    }

    @Override
    public void deleteAdvancedProfile(Integer userId) {

        AdvancedProfile advancedProfile = advancedProfileRepository
                .findByUser_UserId(userId)
                .orElseThrow(() ->
                        new ApiException(ErrorCode.ADVANCED_PROFILE_NOT_FOUND));

        advancedProfileRepository.delete(advancedProfile);
    }

    private UserProfile getUserProfile(Integer userId) {

        UserProfile userProfile = userProfileRepository
                .findByUser_UserId(userId)
                .orElseThrow(() ->
                        new ApiException(ErrorCode.USER_PROFILE_NOT_FOUND));

        validateUserProfile(userProfile);

        return userProfile;
    }

    private double calculateDailyCalories(UserProfile userProfile) {

        return CalorieCalculator.calculateDailyCalories(
                userProfile.getGender(),
                userProfile.getAge(),
                userProfile.getHeightCm(),
                userProfile.getWeightKg(),
                userProfile.getActivityLevel(),
                userProfile.getGoal()
        );
    }

    private void updateUserProfile(
            UserProfile userProfile,
            Double targetWeight,
            Double dailyCalories,
            java.util.List<String> medicalConditions,
            java.util.List<String> allergies
    ) {

        userProfile.setTargetWeightKg(targetWeight);
        userProfile.setBaseTargetCalorie(dailyCalories);
        userProfile.setMedicalConditions(medicalConditions);
        userProfile.setAllergies(allergies);

        userProfileRepository.save(userProfile);
    }

    private void validateUserProfile(UserProfile profile) {

        if (profile.getGender() == null) {
            throw new ApiException(
                    ErrorCode.INVALID_USER_PROFILE,
                    "Gender is required"
            );
        }

        if (profile.getAge() == null || profile.getAge() <= 0) {
            throw new ApiException(
                    ErrorCode.INVALID_USER_PROFILE,
                    "Age is invalid"
            );
        }

        if (profile.getHeightCm() == null || profile.getHeightCm() <= 0) {
            throw new ApiException(
                    ErrorCode.INVALID_USER_PROFILE,
                    "Height is invalid"
            );
        }

        if (profile.getWeightKg() == null || profile.getWeightKg() <= 0) {
            throw new ApiException(
                    ErrorCode.INVALID_USER_PROFILE,
                    "Weight is invalid"
            );
        }

        if (profile.getGoal() == null) {
            throw new ApiException(
                    ErrorCode.INVALID_USER_PROFILE,
                    "Goal is required"
            );
        }

        if (profile.getActivityLevel() == null) {
            throw new ApiException(
                    ErrorCode.INVALID_USER_PROFILE,
                    "Activity level is required"
            );
        }
    }

    private AdvancedProfileResponse mapToResponse(
            AdvancedProfile advancedProfile,
            UserProfile userProfile
    ) {

        return AdvancedProfileResponse.builder()
                .advancedProfileId(advancedProfile.getAdvancedProfileId())
                .userId(advancedProfile.getUser().getUserId())
                .initialWeight(advancedProfile.getInitialWeight())
                .targetWeight(advancedProfile.getTargetWeight())
                .targetDate(advancedProfile.getTargetDate())
                .dailyTargetCalorie(advancedProfile.getDailyTargetCalorie())

                .medicalConditions(userProfile.getMedicalConditions())
                .allergies(userProfile.getAllergies())

                .createdAt(advancedProfile.getCreatedAt())
                .updatedAt(advancedProfile.getUpdatedAt())
                .build();
    }
}