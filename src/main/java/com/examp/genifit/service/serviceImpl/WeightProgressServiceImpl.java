package com.examp.genifit.service.serviceImpl;

import com.examp.genifit.common.exception.ApiException;
import com.examp.genifit.common.exception.ErrorCode;
import com.examp.genifit.dto.request.UpdateWeightProgressRequest;
import com.examp.genifit.dto.response.WeightProgressHistoryResponse;
import com.examp.genifit.dto.response.WeightProgressResponse;
import com.examp.genifit.entity.ProgressStatus;
import com.examp.genifit.entity.User;
import com.examp.genifit.entity.UserProfile;
import com.examp.genifit.entity.WeightProgress;
import com.examp.genifit.repository.UserProfileRepository;
import com.examp.genifit.repository.UserRepository;
import com.examp.genifit.repository.WeightProgressRepository;
import com.examp.genifit.service.WeightProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class WeightProgressServiceImpl implements WeightProgressService {

    private static final double ON_TRACK_THRESHOLD_PERCENT = 5.0;
    private static final double MAINTAIN_TOLERANCE_KG = 0.5;

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final WeightProgressRepository weightProgressRepository;

    // ========================
    // PUBLIC METHODS
    // ========================

    @Override
    @Transactional
    public WeightProgressResponse updateWeightProgress(Integer userId,
                                                       UpdateWeightProgressRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        UserProfile userProfile = userProfileRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_PROFILE_NOT_FOUND));

        LocalDate recordedDate = LocalDate.now();
        double currentWeight = request.getCurrentWeight();

        Double startWeight  = userProfile.getInitialWeight();
        Double targetWeight = userProfile.getTargetWeightKg();
        LocalDate startDate = userProfile.getGoalStartDate();
        LocalDate targetDate = userProfile.getTargetDate();

        // Xác định loại goal của user
        GoalType goalType = resolveGoalType(startWeight, targetWeight, targetDate, startDate);

        // Tính progress theo từng loại goal
        ProgressResult progressResult = calculateProgress(
                goalType, startWeight, targetWeight,
                startDate, targetDate, recordedDate, currentWeight
        );

        // Lưu WeightProgress
        WeightProgress weightProgress = weightProgressRepository
                .findByUser_UserIdAndRecordedDate(userId, recordedDate)
                .orElseGet(() -> {
                    WeightProgress progress = new WeightProgress();
                    progress.setUser(user);
                    progress.setRecordedDate(recordedDate);
                    return progress;
                });

        weightProgress.setCurrentWeight(currentWeight);
        weightProgress.setProgressPercent(progressResult.actualProgressPercent());
        weightProgress.setProgressStatus(progressResult.progressStatus());

        WeightProgress savedProgress = weightProgressRepository.save(weightProgress);

        // Cập nhật cân nặng hiện tại trong profile
        userProfile.setWeightKg(currentWeight);
        userProfileRepository.save(userProfile);

        return buildResponse(savedProgress, userId, startWeight, targetWeight,
                targetDate, progressResult);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WeightProgressHistoryResponse> getWeightProgressHistory(
            Integer userId, Integer pageNum, Integer pageSize) {

        if (!userRepository.existsById(userId)) {
            throw new ApiException(ErrorCode.USER_NOT_FOUND);
        }

        Pageable pageable = PageRequest.of(
                pageNum, pageSize,
                Sort.by(Sort.Direction.DESC, "recordedDate")
                        .and(Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        return weightProgressRepository
                .findAllByUser_UserId(userId, pageable)
                .map(this::mapToWeightProgressHistoryResponse);
    }

    // ========================
    // GOAL TYPE
    // ========================

    private GoalType resolveGoalType(Double startWeight, Double targetWeight,
                                     LocalDate targetDate, LocalDate startDate) {
        // Thiếu thông tin => không có goal
        if (startWeight == null || targetWeight == null
                || targetDate == null || startDate == null) {
            return GoalType.NO_GOAL;
        }

        double diff = targetWeight - startWeight;

        if (Math.abs(diff) < 0.01) { // dùng epsilon thay vì == để tránh float precision
            return GoalType.MAINTAINING;
        }

        return diff < 0 ? GoalType.LOSE_WEIGHT : GoalType.GAIN_WEIGHT;
    }

    // ========================
    // CALCULATE PROGRESS
    // ========================

    private ProgressResult calculateProgress(GoalType goalType,
                                             Double startWeight, Double targetWeight,
                                             LocalDate startDate, LocalDate targetDate,
                                             LocalDate recordedDate, double currentWeight) {
        return switch (goalType) {
            case LOSE_WEIGHT, GAIN_WEIGHT ->
                    calculateWeightChangeProgress(
                            goalType, startWeight, targetWeight,
                            startDate, targetDate, recordedDate, currentWeight
                    );
            case MAINTAINING ->
                    calculateMaintainProgress(targetWeight, currentWeight);

            case NO_GOAL ->
                    ProgressResult.noGoal();
        };
    }

    private ProgressResult calculateWeightChangeProgress(GoalType goalType,
                                                         Double startWeight, Double targetWeight,
                                                         LocalDate startDate, LocalDate targetDate,
                                                         LocalDate recordedDate, double currentWeight) {
        validateWeightChangeConfig(targetDate, recordedDate);

        double expectedPercent = calculateExpectedProgressPercent(startDate, targetDate, recordedDate);
        double actualPercent = calculateActualProgressPercent(startWeight, targetWeight, currentWeight);
        double differencePercent = roundOneDecimal(actualPercent - expectedPercent);

        ProgressStatus status = resolveWeightChangeStatus(differencePercent);

        return new ProgressResult(
                expectedPercent,
                actualPercent,
                differencePercent,
                status,
                buildWeightChangeMessage(status, differencePercent)
        );
    }

    private ProgressResult calculateMaintainProgress(Double targetWeight, double currentWeight) {
        double deviation = Math.abs(currentWeight - targetWeight);
        boolean isOnTrack = deviation <= MAINTAIN_TOLERANCE_KG;

        ProgressStatus status = isOnTrack
                ? ProgressStatus.MAINTAINING
                : ProgressStatus.OUT_OF_RANGE;

        String message = isOnTrack
                ? "Bạn đang giữ cân ổn định. Tiếp tục duy trì nhé!"
                : String.format("Cân nặng của bạn đang lệch %.1f kg so với mục tiêu giữ dáng.", deviation);

        return new ProgressResult(null, null, null, status, message);
    }

    // ========================
    // VALIDATE
    // ========================

    private void validateWeightChangeConfig(LocalDate targetDate, LocalDate recordedDate) {
        if (!targetDate.isAfter(recordedDate)) {
            throw new ApiException(
                    ErrorCode.INVALID_WEIGHT_PROGRESS_CONFIG,
                    "Ngày mục tiêu phải sau ngày hôm nay. Vui lòng cập nhật lại mục tiêu."
            );
        }
    }

    // ========================
    // CALCULATE HELPERS
    // ========================

    private double calculateExpectedProgressPercent(LocalDate startDate,
                                                    LocalDate targetDate,
                                                    LocalDate currentDate) {
        long totalDays = ChronoUnit.DAYS.between(startDate, targetDate);
        long passedDays = ChronoUnit.DAYS.between(startDate, currentDate);

        if (totalDays <= 0) {
            return 100.0;
        }

        double percent = passedDays * 100.0 / totalDays;
        return roundOneDecimal(clamp(percent, 0.0, 100.0));
    }

    private double calculateActualProgressPercent(Double startWeight,
                                                  Double targetWeight,
                                                  double currentWeight) {
        double totalChange = Math.abs(targetWeight - startWeight);

        if (totalChange == 0) {
            return 100.0;
        }

        double actualChange = targetWeight < startWeight
                ? startWeight - currentWeight
                : currentWeight - startWeight;

        double percent = actualChange * 100.0 / totalChange;
        return roundOneDecimal(clamp(percent, 0.0, 100.0));
    }

    private ProgressStatus resolveWeightChangeStatus(double differencePercent) {
        if (differencePercent > ON_TRACK_THRESHOLD_PERCENT) {
            return ProgressStatus.FASTER;
        }
        if (differencePercent < -ON_TRACK_THRESHOLD_PERCENT) {
            return ProgressStatus.SLOWER;
        }
        return ProgressStatus.ON_TRACK;
    }

    private String buildWeightChangeMessage(ProgressStatus status, double differencePercent) {
        double abs = Math.abs(differencePercent);
        return switch (status) {
            case FASTER   -> String.format("Bạn đang nhanh hơn tiến độ %.1f%%!", abs);
            case SLOWER   -> String.format("Bạn đang chậm hơn tiến độ %.1f%%. Cố lên nhé!", abs);
            case ON_TRACK -> "Bạn đang theo đúng tiến độ. Tiếp tục phát huy!";
            default       -> "";
        };
    }

    // ========================
    // BUILD RESPONSE
    // ========================

    private WeightProgressResponse buildResponse(WeightProgress savedProgress,
                                                 Integer userId,
                                                 Double startWeight,
                                                 Double targetWeight,
                                                 LocalDate targetDate,
                                                 ProgressResult result) {
        return WeightProgressResponse.builder()
                .progressId(savedProgress.getProgressId())
                .userId(userId)
                .recordedDate(savedProgress.getRecordedDate())
                .startWeight(startWeight)
                .currentWeight(savedProgress.getCurrentWeight())
                .targetWeight(targetWeight)
                .targetDate(targetDate)
                .expectedProgressPercent(result.expectedProgressPercent())
                .actualProgressPercent(result.actualProgressPercent())
                .differencePercent(result.differencePercent())
                .progressStatus(result.progressStatus())
                .message(result.message())
                .build();
    }

    private WeightProgressHistoryResponse mapToWeightProgressHistoryResponse(WeightProgress progress) {
        return WeightProgressHistoryResponse.builder()
                .progressId(progress.getProgressId())
                .recordedDate(progress.getRecordedDate())
                .currentWeight(progress.getCurrentWeight())
                .progressPercent(progress.getProgressPercent())
                .progressStatus(progress.getProgressStatus())
                .createdAt(progress.getCreatedAt())
                .build();
    }

    // ========================
    // UTILS
    // ========================

    private double roundOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    // ========================
    // INNER TYPES
    // ========================
    private enum GoalType {
        LOSE_WEIGHT,
        GAIN_WEIGHT,
        MAINTAINING,
        NO_GOAL
    }

    private record ProgressResult(
            Double expectedProgressPercent,
            Double actualProgressPercent,
            Double differencePercent,
            ProgressStatus progressStatus,
            String message
    ) {
        static ProgressResult noGoal() {
            return new ProgressResult(
                    null, null, null,
                    ProgressStatus.NO_GOAL,
                    "Cân nặng đã được cập nhật thành công."
            );
        }
    }
}