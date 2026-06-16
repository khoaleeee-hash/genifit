package com.examp.genifit.service.serviceImpl;

import com.examp.genifit.common.exception.ApiException;
import com.examp.genifit.common.exception.ErrorCode;
import com.examp.genifit.dto.response.*;
import com.examp.genifit.dto.request.AddManualFoodRequest;
import com.examp.genifit.entity.*;
import com.examp.genifit.repository.*;
import com.examp.genifit.service.DailyLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class DailyLogServiceImpl implements DailyLogService {

    private final FoodItemRepository foodItemRepository;
    private final DailyLogRepository dailyLogRepository;
    private final GuestRepository guestRepository;
    private final UserRepository userRepository;
    private final LogDetailRepository logDetailRepository;

    @Override
    public AddManualFoodResponse addManualFood(AddManualFoodRequest request) {

        if (request.getUserId() == null && request.getGuestId() == null) {
            throw new RuntimeException("Cần truyền userId hoặc guestId");
        }

        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new RuntimeException("Số lượng món ăn phải lớn hơn 0");
        }

        FoodItem foodItem = findFoodItem(request);

        LocalDate today = LocalDate.now();
        DailyLog dailyLog;

        if (request.getUserId() != null) {
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

            dailyLog = dailyLogRepository
                    .findByUser_UserIdAndLogDate(request.getUserId(), today)
                    .orElseGet(() -> createDailyLogForUser(user));
        } else {
            Guest guest = guestRepository.findById(request.getGuestId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy guest"));

            dailyLog = dailyLogRepository
                    .findByGuest_GuestIdAndLogDate(request.getGuestId(), today)
                    .orElseGet(() -> createDailyLogForGuest(guest));
        }

        MealTime mealTime = request.getMealTime() == null
                ? MealTime.SNACK
                : request.getMealTime();

        boolean isDuplicate = logDetailRepository
                .existsByDailyLog_LogIdAndFoodItem_FoodIdAndMealTime(
                        dailyLog.getLogId(),
                        foodItem.getFoodId(),
                        mealTime
                );

        double addedCalories = foodItem.getCalories() * request.getQuantity();

        LogDetail logDetail = new LogDetail();
        logDetail.setDailyLog(dailyLog);
        logDetail.setFoodItem(foodItem);
        logDetail.setQuantity(request.getQuantity());
        logDetail.setCalories(addedCalories);
        logDetail.setSource(FoodSource.MANUAL);
        logDetail.setMealTime(mealTime);
        logDetail.setCreatedAt(LocalDateTime.now());

        logDetailRepository.save(logDetail);

        double currentTotal = dailyLog.getTotalCalories() == null
                ? 0
                : dailyLog.getTotalCalories();

        double newTotal = currentTotal + addedCalories;

        dailyLog.setTotalCalories(newTotal);
        dailyLog.setStatusColor(calculateStatusColor(newTotal, dailyLog.getTargetCalories()));

        dailyLogRepository.save(dailyLog);

        String duplicateMessage = null;

        if (isDuplicate) {
            duplicateMessage = "Bạn đã nhập món này trong bữa " + mealTime + " hôm nay";
        }

        String message = isDuplicate
                ? "Thêm món ăn thành công, nhưng món này đã từng được nhập trong bữa này hôm nay"
                : "Thêm món ăn thành công";

        return new AddManualFoodResponse(
                message,
                foodItem.getFoodName(),
                request.getQuantity(),
                addedCalories,
                newTotal,
                dailyLog.getStatusColor().name(),
                isDuplicate,
                duplicateMessage
        );
    }

    @Override
    @Transactional(readOnly = true)
    public DailyCaloriesResponse getTodayCalories(Integer userId) {

        DailyLog dailyLog = dailyLogRepository.findByUser_UserIdAndLogDate(userId, LocalDate.now())
                .orElseThrow(() -> new ApiException(ErrorCode.DAILY_LOG_NOT_FOUND));

        return DailyCaloriesResponse.builder()
                .date(dailyLog.getLogDate())
                .totalCalories(dailyLog.getTotalCalories())
                .targetCalories(dailyLog.getTargetCalories())
                .statusColor(dailyLog.getStatusColor())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public DailyLogResponse getCaloriesByDate(Integer userId, LocalDate date) {

        DailyLog dailyLog = dailyLogRepository.findByUser_UserIdAndLogDate(userId, date)
                .orElseThrow(() -> new ApiException(ErrorCode.DAILY_LOG_NOT_FOUND));

        List<DailyLogResponse.FoodDetail> foods = dailyLog.getLogDetails()
                .stream()
                .map(detail ->
                        DailyLogResponse.FoodDetail
                                .builder()
                                .foodName(detail.getFoodItem().getFoodName())
                                .quantity(detail.getQuantity())
                                .calories(detail.getCalories())
                                .mealTime(detail.getMealTime())
                                .build()
                )
                .toList();

        return DailyLogResponse.builder()
                .date(dailyLog.getLogDate())
                .totalCalories(dailyLog.getTotalCalories())
                .targetCalories(dailyLog.getTargetCalories())
                .statusColor(dailyLog.getStatusColor())
                .foods(foods)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public HomeStatusResponse getHomeStatus(Integer userId) {

        LocalDate today = LocalDate.now();

        DailyLog dailyLog = dailyLogRepository.findByUser_UserIdAndLogDate(userId, today)
                .orElseThrow(() -> new ApiException(ErrorCode.DAILY_LOG_NOT_FOUND));

        Double totalCalories = safeDouble(dailyLog.getTotalCalories());
        Double targetCalories = resolveTargetCalories(dailyLog.getTargetCalories());

        double progressPercent = calculateProgressPercent(totalCalories, targetCalories);

        StatusColor statusColor = calculateStatusColor(totalCalories, targetCalories);

        return HomeStatusResponse.builder()
                .totalCalories(totalCalories)
                .targetCalories(targetCalories)
                .progressPercent(progressPercent)
                .statusColor(statusColor)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public WeeklyChartResponse getWeeklyChart(Integer userId) {

        if (!userRepository.existsById(userId)) {
            throw new ApiException(ErrorCode.USER_NOT_FOUND);
        }

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6);

        List<DailyLog> logs = dailyLogRepository.findAllByUser_UserIdAndLogDateBetweenOrderByLogDateAsc(userId, startDate, endDate
        );

        Map<LocalDate, DailyLog> logMap = logs.stream()
                .collect(Collectors.toMap(DailyLog::getLogDate, Function.identity()));

        List<WeeklyChartPointResponse> points =
                IntStream.rangeClosed(0, 6).mapToObj(index -> {
                            LocalDate date = startDate.plusDays(index);
                            DailyLog log = logMap.get(date);

                            if (log == null) {
                                return buildEmptyChartPoint(date);
                            }

                            return mapToWeeklyChartPoint(log);
                        })
                        .toList();

        return WeeklyChartResponse.builder()
                .range(LocalDateRangeResponse.builder()
                        .startDate(startDate)
                        .endDate(endDate)
                        .build()
                )
                .points(points)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DailySummaryResponse> getMonthlyLogs(Integer userId, Integer year, Integer month) {
        validateYearAndMonth(year, month);

        if (!userRepository.existsById(userId)) {
            throw new ApiException(ErrorCode.USER_NOT_FOUND);
        }

        YearMonth yearMonth = YearMonth.of(year, month);

        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<DailyLog> logs = dailyLogRepository
                .findAllByUser_UserIdAndLogDateBetweenOrderByLogDateAsc(userId, startDate, endDate);

        return logs.stream()
                .map(this::mapToDailySummaryResponse)
                .toList();
    }

    private FoodItem findFoodItem(AddManualFoodRequest request) {

        if (request.getFoodId() != null) {
            return foodItemRepository.findById(request.getFoodId())
                    .filter(food -> !Boolean.TRUE.equals(food.getDeleted()))
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy món ăn"));
        }

        if (request.getFoodName() != null && !request.getFoodName().trim().isEmpty()) {
            return foodItemRepository.findByFoodNameIgnoreCaseAndDeletedFalse(request.getFoodName().trim())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy món ăn có tên: " + request.getFoodName()));
        }

        throw new RuntimeException("Cần truyền foodId hoặc foodName");
    }

    private DailyLog createDailyLogForUser(User user) {
        DailyLog dailyLog = new DailyLog();
        dailyLog.setUser(user);
        dailyLog.setLogDate(LocalDate.now());
        dailyLog.setTotalCalories(0.0);
        dailyLog.setTargetCalories(2000.0);
        dailyLog.setStatusColor(StatusColor.BLUE);
        dailyLog.setCreatedAt(LocalDateTime.now());

        return dailyLogRepository.save(dailyLog);
    }

    private DailyLog createDailyLogForGuest(Guest guest) {
        DailyLog dailyLog = new DailyLog();
        dailyLog.setGuest(guest);
        dailyLog.setLogDate(LocalDate.now());
        dailyLog.setTotalCalories(0.0);
        dailyLog.setTargetCalories(2000.0);
        dailyLog.setStatusColor(StatusColor.BLUE);
        dailyLog.setCreatedAt(LocalDateTime.now());

        return dailyLogRepository.save(dailyLog);
    }

    private StatusColor calculateStatusColor(Double totalCalories, Double targetCalories) {

        if (totalCalories == null) {
            totalCalories = 0.0;
        }

        if (targetCalories == null || targetCalories <= 0) {
            targetCalories = 2000.0;
        }

        double percent = totalCalories / targetCalories;

        if (percent < 0.9) {
            return StatusColor.BLUE;
        } else if (percent <= 1.05) {
            return StatusColor.GREEN;
        } else if (percent <= 1.2) {
            return StatusColor.YELLOW;
        } else {
            return StatusColor.RED;
        }
    }

    //Helper cho monthly và weekly chart
    private DailySummaryResponse mapToDailySummaryResponse(DailyLog log) {

        Double totalCalories = safeDouble(log.getTotalCalories());
        Double targetCalories = resolveTargetCalories(log.getTargetCalories());
        Double progressPercent = calculateProgressPercent(
                totalCalories,
                targetCalories
        );

        StatusColor statusColor = log.getStatusColor() != null
                ? log.getStatusColor()
                : calculateStatusColor(totalCalories, targetCalories);

        return DailySummaryResponse.builder()
                .date(log.getLogDate())
                .totalCalories(totalCalories)
                .targetCalories(targetCalories)
                .progressPercent(progressPercent)
                .statusColor(statusColor)
                .build();
    }

    private WeeklyChartPointResponse mapToWeeklyChartPoint(DailyLog log) {

        Double totalCalories = safeDouble(log.getTotalCalories());
        Double targetCalories = resolveTargetCalories(log.getTargetCalories());
        Double progressPercent = calculateProgressPercent(
                totalCalories,
                targetCalories
        );

        StatusColor statusColor = log.getStatusColor() != null
                ? log.getStatusColor()
                : calculateStatusColor(totalCalories, targetCalories);

        return WeeklyChartPointResponse.builder()
                .date(log.getLogDate())
                .label(formatChartLabel(log.getLogDate()))
                .totalCalories(totalCalories)
                .targetCalories(targetCalories)
                .progressPercent(progressPercent)
                .statusColor(statusColor)
                .build();
    }

    private WeeklyChartPointResponse buildEmptyChartPoint(LocalDate date) {

        Double totalCalories = 0.0;
        Double targetCalories = 2000.0;

        return WeeklyChartPointResponse.builder()
                .date(date)
                .label(formatChartLabel(date))
                .totalCalories(totalCalories)
                .targetCalories(targetCalories)
                .progressPercent(0.0)
                .statusColor(StatusColor.BLUE)
                .build();
    }

    private String formatChartLabel(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern("MM-dd"));
    }

    private Double safeDouble(Double value) {
        return value == null ? 0.0 : value;
    }

    private Double resolveTargetCalories(Double targetCalories) {
        return targetCalories == null || targetCalories <= 0
                ? 2000.0
                : targetCalories;
    }

    private Double calculateProgressPercent(Double totalCalories, Double targetCalories) {
        if (targetCalories == null || targetCalories <= 0) {
            return 0.0;
        }

        double percent = totalCalories / targetCalories * 100;

        return Math.round(percent * 10.0) / 10.0;
    }

    private void validateYearAndMonth(Integer year, Integer month) {
        if (year == null || year < 2000 || year > 2100) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Year must be between 2000 and 2100");
        }

        if (month == null || month < 1 || month > 12) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Month must be between 1 and 12");
        }
    }
}