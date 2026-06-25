package com.examp.genifit.service.serviceImpl;

import com.examp.genifit.common.exception.ApiException;
import com.examp.genifit.common.exception.ErrorCode;
import com.examp.genifit.dto.request.UpdateWeightProgressRequest;
import com.examp.genifit.dto.response.WeightProgressHistoryResponse;
import com.examp.genifit.dto.response.WeightProgressResponse;
import com.examp.genifit.entity.AdvancedProfile;
import com.examp.genifit.entity.ProgressStatus;
import com.examp.genifit.entity.User;
import com.examp.genifit.entity.UserProfile;
import com.examp.genifit.entity.WeightProgress;
import com.examp.genifit.repository.AdvancedProfileRepository;
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

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final AdvancedProfileRepository advancedProfileRepository;
    private final WeightProgressRepository weightProgressRepository;

    @Override
    @Transactional
    public WeightProgressResponse updateWeightProgress(UpdateWeightProgressRequest request) {
        if (request.getCurrentWeight() == null || request.getCurrentWeight() <= 0) {
            throw new ApiException(ErrorCode.INVALID_WEIGHT_VALUE, "Current weight must be greater than 0");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        UserProfile userProfile = userProfileRepository.findByUser_UserId(request.getUserId())
                .orElseThrow(() -> new ApiException(ErrorCode.USER_PROFILE_NOT_FOUND));

        AdvancedProfile advancedProfile = advancedProfileRepository.findByUser_UserId(request.getUserId())
                .orElseThrow(() -> new ApiException(ErrorCode.ADVANCED_PROFILE_NOT_FOUND));

        Double startWeight = userProfile.getWeightKg();
        Double targetWeight = advancedProfile.getTargetWeight();
        LocalDate targetDate = advancedProfile.getTargetDate();
        LocalDate recordedDate = LocalDate.now();

        validateProgressConfig(startWeight, targetWeight, targetDate, recordedDate);

        double expectedProgressPercent = calculateExpectedProgressPercent(advancedProfile.getCreatedAt().toLocalDate(),
                targetDate, recordedDate);

        double actualProgressPercent = calculateActualProgressPercent(startWeight, targetWeight, request.getCurrentWeight());

        double differencePercent = roundOneDecimal(actualProgressPercent - expectedProgressPercent);

        ProgressStatus progressStatus = resolveProgressStatus(differencePercent);

        WeightProgress weightProgress = weightProgressRepository.findByUser_UserIdAndRecordedDate(request.getUserId(), recordedDate)
                .orElseGet(() -> {
                    WeightProgress progress = new WeightProgress();
                    progress.setUser(user);
                    progress.setRecordedDate(recordedDate);
                    return progress;
                });

        weightProgress.setCurrentWeight(request.getCurrentWeight());
        weightProgress.setProgressPercent(actualProgressPercent);
        weightProgress.setProgressStatus(progressStatus);

        WeightProgress savedProgress = weightProgressRepository.save(weightProgress);

        return WeightProgressResponse.builder()
                .progressId(savedProgress.getProgressId())
                .userId(user.getUserId())
                .recordedDate(savedProgress.getRecordedDate())
                .startWeight(startWeight)
                .currentWeight(savedProgress.getCurrentWeight())
                .targetWeight(targetWeight)
                .targetDate(targetDate)
                .expectedProgressPercent(expectedProgressPercent)
                .actualProgressPercent(actualProgressPercent)
                .differencePercent(differencePercent)
                .progressStatus(progressStatus)
                .message(buildProgressMessage(progressStatus, differencePercent))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WeightProgressHistoryResponse> getWeightProgressHistory(Integer userId, Integer pageNum, Integer pageSize) {
        if (!userRepository.existsById(userId)) {
            throw new ApiException(ErrorCode.USER_NOT_FOUND);
        }

        Pageable pageable = PageRequest.of(pageNum, pageSize,
                Sort.by(Sort.Direction.DESC, "recordedDate")
                        .and(Sort.by(Sort.Direction.DESC, "createdAt")));

        return weightProgressRepository.findAllByUser_UserId(userId, pageable)
                .map(this::mapToWeightProgressHistoryResponse);
    }

    private void validateProgressConfig(Double startWeight, Double targetWeight,
                                        LocalDate targetDate, LocalDate recordedDate) {
        if (startWeight == null || startWeight <= 0) {
            throw new ApiException(ErrorCode.INVALID_WEIGHT_PROGRESS_CONFIG, "Start weight is missing or invalid");
        }

        if (targetWeight == null || targetWeight <= 0) {
            throw new ApiException(ErrorCode.INVALID_WEIGHT_PROGRESS_CONFIG, "Target weight is missing or invalid");
        }

        if (targetDate == null) {
            throw new ApiException(ErrorCode.INVALID_WEIGHT_PROGRESS_CONFIG, "Target date is required");
        }

        if (!targetDate.isAfter(recordedDate)) {
            throw new ApiException(ErrorCode.INVALID_WEIGHT_PROGRESS_CONFIG, "Target date must be after today");
        }

        if (startWeight.equals(targetWeight)) {
            throw new ApiException(ErrorCode.INVALID_WEIGHT_PROGRESS_CONFIG, "Start weight and target weight must be different");
        }
    }

    private double calculateExpectedProgressPercent(LocalDate startDate, LocalDate targetDate, LocalDate currentDate) {
        long totalDays = ChronoUnit.DAYS.between(startDate, targetDate);

        long passedDays = ChronoUnit.DAYS.between(startDate, currentDate);

        if (totalDays <= 0) {
            return 100.0;
        }

        double percent = passedDays * 100.0 / totalDays;

        if (percent < 0) {
            percent = 0.0;
        }

        if (percent > 100) {
            percent = 100.0;
        }

        return roundOneDecimal(percent);
    }

    private double calculateActualProgressPercent(Double startWeight, Double targetWeight, Double currentWeight) {
        double totalChangeNeeded = Math.abs(startWeight - targetWeight);

        if (totalChangeNeeded == 0) {
            return 100.0;
        }

        double actualChange;

        if (targetWeight < startWeight) {
            // Lose weight
            actualChange = startWeight - currentWeight;
        } else {
            // Gain weight
            actualChange = currentWeight - startWeight;
        }

        double percent = actualChange * 100.0 / totalChangeNeeded;

        if (percent < 0) {
            percent = 0.0;
        }

        if (percent > 100) {
            percent = 100.0;
        }

        return roundOneDecimal(percent);
    }

    private ProgressStatus resolveProgressStatus(double differencePercent) {
        if (differencePercent > ON_TRACK_THRESHOLD_PERCENT) {
            return ProgressStatus.FASTER;
        }

        if (differencePercent < -ON_TRACK_THRESHOLD_PERCENT) {
            return ProgressStatus.SLOWER;
        }

        return ProgressStatus.ON_TRACK;
    }

    private String buildProgressMessage(ProgressStatus status, double differencePercent) {
        double absDifference = Math.abs(differencePercent);

        return switch (status) {
            case FASTER -> "Bạn đang nhanh hơn tiến độ " + absDifference + "%";
            case SLOWER -> "Bạn đang chậm hơn tiến độ " + absDifference + "%";
            case ON_TRACK -> "Bạn đang theo đúng tiến độ";
        };
    }

    private double roundOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private WeightProgressHistoryResponse mapToWeightProgressHistoryResponse(
            WeightProgress progress
    ) {
        return WeightProgressHistoryResponse.builder()
                .progressId(progress.getProgressId())
                .recordedDate(progress.getRecordedDate())
                .currentWeight(progress.getCurrentWeight())
                .progressPercent(progress.getProgressPercent())
                .progressStatus(progress.getProgressStatus())
                .createdAt(progress.getCreatedAt())
                .build();
    }
}