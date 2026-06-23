package com.examp.genifit.service.serviceImpl;

import com.examp.genifit.common.exception.ApiException;
import com.examp.genifit.common.exception.ErrorCode;
import com.examp.genifit.dto.info.ResolvedFoodInfo;
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
    private final AIScanHistoryRepository aiScanHistoryRepository;

    @Override
    @Transactional
    public AddManualFoodResponse addManualFood(String username, AddManualFoodRequest request) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user đang đăng nhập"));

        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new IllegalArgumentException("Số lượng món ăn phải lớn hơn 0");
        }

        ResolvedFoodInfo foodInfo = resolveFoodInfo(user, request);

        LocalDate today = LocalDate.now();

        DailyLog dailyLog = dailyLogRepository
                .findByUser_UserIdAndLogDate(user.getUserId(), today)
                .orElseGet(() -> createDailyLogForUser(user));

        MealTime mealTime = request.getMealTime() == null
                ? MealTime.SNACK
                : request.getMealTime();

        FoodItem foodItem = null;

        if (foodInfo.getFoodId() != null) {
            foodItem = foodItemRepository.findById(foodInfo.getFoodId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy món ăn với foodId: " + foodInfo.getFoodId()));
        }

        boolean isDuplicate = false;

        if (foodInfo.getFoodId() != null) {
            isDuplicate = logDetailRepository
                    .existsByDailyLog_LogIdAndFoodItem_FoodIdAndMealTime(
                            dailyLog.getLogId(),
                            foodInfo.getFoodId(),
                            mealTime
                    );
        }
        else {
            isDuplicate = logDetailRepository
                    .existsByDailyLog_LogIdAndFoodNameSnapshotIgnoreCaseAndMealTime(
                            dailyLog.getLogId(),
                            foodInfo.getFoodName(),
                            mealTime
                    );
        }

        double quantity = request.getQuantity();

        double addedCalories = safeDouble(foodInfo.getCalories()) * quantity;
        double addedFat = safeDouble(foodInfo.getFat()) * quantity;
        double addedCarbs = safeDouble(foodInfo.getCarbs()) * quantity;
        double addedProtein = safeDouble(foodInfo.getProtein()) * quantity;

        LogDetail logDetail = new LogDetail();
        logDetail.setDailyLog(dailyLog);

        logDetail.setFoodItem(foodItem);

        logDetail.setFoodNameSnapshot(foodInfo.getFoodName());
        logDetail.setQuantity(quantity);
        logDetail.setCalories(addedCalories);
        logDetail.setFat(addedFat);
        logDetail.setCarbs(addedCarbs);
        logDetail.setProtein(addedProtein);
        if (foodInfo.getScanId() != null) {
            logDetail.setSource(FoodSource.SCAN);
        } else {
            logDetail.setSource(FoodSource.MANUAL);
        }
        logDetail.setMealTime(mealTime);
        logDetail.setCreatedAt(LocalDateTime.now());

        if(foodInfo.getScanId() != null) {
            AIScanHistory scanHistory = aiScanHistoryRepository.findById(foodInfo.getScanId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch sử scan với scanId: " + foodInfo.getScanId()));

            logDetail.setScanHistory(scanHistory);
        }
        logDetailRepository.save(logDetail);

        double currentTotal = safeDouble(dailyLog.getTotalCalories());
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
                foodInfo.getFoodName(),
                quantity,
                addedCalories,
                newTotal,
                dailyLog.getStatusColor().name(),
                isDuplicate,
                duplicateMessage
        );
    }

    @Override
    @Transactional(readOnly = true)
    public MealHistoryResponse getMealHistory(String username, LocalDate date) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user đang đăng nhập"));

        LocalDate targetDate = date == null ? LocalDate.now() : date;

        DailyLog dailyLog = dailyLogRepository
                .findByUser_UserIdAndLogDate(user.getUserId(), targetDate)
                .orElseThrow(() -> new ApiException(ErrorCode.DAILY_LOG_NOT_FOUND));

        List<MealHistoryResponse.MealItem> meals = dailyLog.getLogDetails()
                .stream()
                .map(this::mapToMealHistoryItem)
                .toList();

        return MealHistoryResponse.builder()
                .date(dailyLog.getLogDate())
                .totalCalories(safeDouble(dailyLog.getTotalCalories()))
                .targetCalories(resolveTargetCalories(dailyLog.getTargetCalories()))
                .statusColor(
                        dailyLog.getStatusColor() != null
                                ? dailyLog.getStatusColor()
                                : calculateStatusColor(dailyLog.getTotalCalories(), dailyLog.getTargetCalories())
                )
                .meals(meals)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public DailyCaloriesResponse getTodayCalories(Integer userId) {

        LocalDate today = LocalDate.now();

        DailyLog dailyLog = dailyLogRepository
                .findByUser_UserIdAndLogDate(userId, today)
                .orElseThrow(() -> new ApiException(ErrorCode.DAILY_LOG_NOT_FOUND));

        NutritionTotals totals = calculateNutritionTotals(dailyLog);

        Double targetCalories = resolveTargetCalories(dailyLog.getTargetCalories());

        Double progressPercent = calculateProgressPercent(
                totals.calories,
                targetCalories
        );

        StatusColor statusColor = calculateStatusColor(
                totals.calories,
                targetCalories
        );

        return DailyCaloriesResponse.builder()
                .date(dailyLog.getLogDate())
                .totalCalories(totals.calories)
                .totalProtein(totals.protein)
                .totalCarbs(totals.carbs)
                .totalFat(totals.fat)
                .targetCalories(targetCalories)
                .progressPercent(progressPercent)
                .statusColor(statusColor)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public DailyLogResponse getCaloriesByDate(
            Integer userId,
            LocalDate date
    ) {

        DailyLog dailyLog = dailyLogRepository
                .findByUser_UserIdAndLogDate(userId, date)
                .orElseThrow(() -> new ApiException(ErrorCode.DAILY_LOG_NOT_FOUND));

        NutritionTotals totals = calculateNutritionTotals(dailyLog);

        Double targetCalories = resolveTargetCalories(dailyLog.getTargetCalories());

        Double progressPercent = calculateProgressPercent(
                totals.calories,
                targetCalories
        );

        StatusColor statusColor = calculateStatusColor(
                totals.calories,
                targetCalories
        );

        List<DailyLogResponse.FoodDetail> foods = dailyLog.getLogDetails()
                .stream()
                .map(detail ->
                        DailyLogResponse.FoodDetail
                                .builder()
                                .foodName(resolveFoodName(detail))
                                .quantity(safeDouble(detail.getQuantity()))
                                .calories(roundOneDecimal(detail.getCalories()))
                                .protein(roundOneDecimal(detail.getProtein()))
                                .carbs(roundOneDecimal(detail.getCarbs()))
                                .fat(roundOneDecimal(detail.getFat()))
                                .mealTime(detail.getMealTime())
                                .build()
                )
                .toList();

        return DailyLogResponse.builder()
                .date(dailyLog.getLogDate())
                .totalCalories(totals.calories)
                .totalProtein(totals.protein)
                .totalCarbs(totals.carbs)
                .totalFat(totals.fat)
                .targetCalories(targetCalories)
                .progressPercent(progressPercent)
                .statusColor(statusColor)
                .foods(foods)
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

        String foodName = request.getFoodName();

        if (foodName == null || foodName.trim().isEmpty()) {
            throw new RuntimeException("Cần truyền tên món ăn");
        }

        return foodItemRepository
                .findByFoodNameIgnoreCaseAndDeletedFalse(foodName.trim())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy món ăn có tên: " + foodName));
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

    private DailySummaryResponse mapToDailySummaryResponse(DailyLog log) {

        NutritionTotals totals = calculateNutritionTotals(log);

        Double targetCalories = resolveTargetCalories(log.getTargetCalories());

        Double progressPercent = calculateProgressPercent(
                totals.calories,
                targetCalories
        );

        StatusColor statusColor = log.getStatusColor() != null
                ? log.getStatusColor()
                : calculateStatusColor(totals.calories, targetCalories);

        return DailySummaryResponse.builder()
                .date(log.getLogDate())
                .totalCalories(totals.calories)
                .totalProtein(totals.protein)
                .totalCarbs(totals.carbs)
                .totalFat(totals.fat)
                .targetCalories(targetCalories)
                .progressPercent(progressPercent)
                .statusColor(statusColor)
                .build();
    }

    private WeeklyChartPointResponse mapToWeeklyChartPoint(DailyLog log) {

        NutritionTotals totals = calculateNutritionTotals(log);

        Double targetCalories = resolveTargetCalories(log.getTargetCalories());

        Double progressPercent = calculateProgressPercent(
                totals.calories,
                targetCalories
        );

        StatusColor statusColor = log.getStatusColor() != null
                ? log.getStatusColor()
                : calculateStatusColor(totals.calories, targetCalories);

        return WeeklyChartPointResponse.builder()
                .date(log.getLogDate())
                .label(formatChartLabel(log.getLogDate()))
                .totalCalories(totals.calories)
                .totalProtein(totals.protein)
                .totalCarbs(totals.carbs)
                .totalFat(totals.fat)
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
                .totalProtein(0.0)
                .totalCarbs(0.0)
                .totalFat(0.0)
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

    private ResolvedFoodInfo resolveFoodInfo(User user, AddManualFoodRequest request) {

        boolean hasFoodId = isValidId(request.getFoodId());
        boolean hasScanId = isValidId(request.getScanId());
        boolean hasFoodName = isRealFoodName(request.getFoodName());

        int sourceCount = 0;

        if (hasFoodId) {
            sourceCount++;
        }

        if (hasScanId) {
            sourceCount++;
        }

        if (hasFoodName) {
            sourceCount++;
        }

        if (sourceCount == 0) {
            throw new IllegalArgumentException(
                    "Vui lòng truyền foodId > 0 hoặc scanId > 0 hoặc foodName hợp lệ"
            );
        }

        if (sourceCount > 1) {
            throw new IllegalArgumentException(
                    "Chỉ được truyền một trong ba: foodId, scanId hoặc foodName"
            );
        }

        if (hasFoodId) {
            return resolveFromFoodItem(request.getFoodId());
        }

        if (hasScanId) {
            return resolveFromScanHistory(user, request.getScanId());
        }

        return resolveFromManualInput(request);
    }

    private ResolvedFoodInfo resolveFromFoodItem(Integer foodId) {

        FoodItem foodItem = foodItemRepository.findByFoodIdAndDeletedFalse(foodId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy món ăn với foodId: " + foodId));

        if (foodItem.getCalories() == null) {
            throw new RuntimeException("Món ăn chưa có thông tin calories");
        }

        return ResolvedFoodInfo.builder()
                .foodId(foodItem.getFoodId())
                .scanId(null)
                .foodName(foodItem.getFoodName())
                .calories(foodItem.getCalories())
                .fat(foodItem.getFat())
                .carbs(foodItem.getCarbs())
                .protein(foodItem.getProtein())
                .build();
    }

    private ResolvedFoodInfo resolveFromScanHistory(User user, Integer scanId) {

        AIScanHistory scanHistory = aiScanHistoryRepository.findById(scanId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch sử scan với scanId: " + scanId));

        if (scanHistory.getUser() == null
                || !scanHistory.getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("Bạn không có quyền sử dụng scanId này");
        }

        if (scanHistory.getDetectedFood() == null
                || scanHistory.getDetectedFood().trim().isEmpty()) {
            throw new RuntimeException("Lịch sử scan chưa có tên món ăn");
        }

        if (scanHistory.getEstimatedCalories() == null) {
            throw new RuntimeException("Lịch sử scan chưa có thông tin calories");
        }

        return ResolvedFoodInfo.builder()
                .foodId(null)
                .scanId(scanHistory.getScanId())
                .foodName(scanHistory.getDetectedFood())
                .calories(scanHistory.getEstimatedCalories())
                .fat(scanHistory.getFat())
                .carbs(scanHistory.getCarbs())
                .protein(scanHistory.getProtein())
                .build();
    }

    private boolean isValidId(Integer id) {
        return id != null && id > 0;
    }

    private boolean isRealFoodName(String foodName) {
        return foodName != null
                && !foodName.trim().isEmpty()
                && !"string".equalsIgnoreCase(foodName.trim());
    }

    private ResolvedFoodInfo resolveFromManualInput(AddManualFoodRequest request) {

        validateManualNutrition(request);

        return ResolvedFoodInfo.builder()
                .foodId(null)
                .scanId(null)
                .foodName(request.getFoodName().trim())
                .calories(request.getCalories())
                .fat(request.getFat())
                .carbs(request.getCarbs())
                .protein(request.getProtein())
                .build();
    }

    private void validateManualNutrition(AddManualFoodRequest request) {

        if (!isRealFoodName(request.getFoodName())) {
            throw new IllegalArgumentException("foodName không hợp lệ");
        }

        if (request.getCalories() == null || request.getCalories() <= 0) {
            throw new IllegalArgumentException("calories is required and must be > 0");
        }

        if (request.getFat() == null || request.getFat() < 0) {
            throw new IllegalArgumentException("fat is required and must be >= 0");
        }

        if (request.getCarbs() == null || request.getCarbs() < 0) {
            throw new IllegalArgumentException("carbs is required and must be >= 0");
        }

        if (request.getProtein() == null || request.getProtein() < 0) {
            throw new IllegalArgumentException("protein is required and must be >= 0");
        }
    }

    private static class NutritionTotals {

        private final Double calories;
        private final Double protein;
        private final Double carbs;
        private final Double fat;

        public NutritionTotals(
                Double calories,
                Double protein,
                Double carbs,
                Double fat
        ) {
            this.calories = calories;
            this.protein = protein;
            this.carbs = carbs;
            this.fat = fat;
        }
    }

    private NutritionTotals calculateNutritionTotals(DailyLog dailyLog) {

        if (dailyLog.getLogDetails() == null || dailyLog.getLogDetails().isEmpty()) {
            return new NutritionTotals(
                    0.0,
                    0.0,
                    0.0,
                    0.0
            );
        }

        double totalCalories = dailyLog.getLogDetails()
                .stream()
                .mapToDouble(detail -> safeDouble(detail.getCalories()))
                .sum();

        double totalProtein = dailyLog.getLogDetails()
                .stream()
                .mapToDouble(detail -> safeDouble(detail.getProtein()))
                .sum();

        double totalCarbs = dailyLog.getLogDetails()
                .stream()
                .mapToDouble(detail -> safeDouble(detail.getCarbs()))
                .sum();

        double totalFat = dailyLog.getLogDetails()
                .stream()
                .mapToDouble(detail -> safeDouble(detail.getFat()))
                .sum();

        return new NutritionTotals(
                roundOneDecimal(totalCalories),
                roundOneDecimal(totalProtein),
                roundOneDecimal(totalCarbs),
                roundOneDecimal(totalFat)
        );
    }

    private Double roundOneDecimal(Double value) {
        if (value == null) {
            return 0.0;
        }

        return Math.round(value * 10.0) / 10.0;
    }

    private String resolveFoodName(LogDetail detail) {

        if (detail.getFoodNameSnapshot() != null
                && !detail.getFoodNameSnapshot().trim().isEmpty()) {
            return detail.getFoodNameSnapshot();
        }

        if (detail.getFoodItem() != null) {
            return detail.getFoodItem().getFoodName();
        }

        return "Unknown food";
    }

    private MealHistoryResponse.MealItem mapToMealHistoryItem(LogDetail detail) {

        Integer foodId = null;
        Integer scanId = null;

        if (detail.getFoodItem() != null) {
            foodId = detail.getFoodItem().getFoodId();
        }

        if (detail.getScanHistory() != null) {
            scanId = detail.getScanHistory().getScanId();
        }

        String foodName = detail.getFoodNameSnapshot();

        if ((foodName == null || foodName.isBlank()) && detail.getFoodItem() != null) {
            foodName = detail.getFoodItem().getFoodName();
        }

        if ((foodName == null || foodName.isBlank()) && detail.getScanHistory() != null) {
            foodName = detail.getScanHistory().getDetectedFood();
        }

        return MealHistoryResponse.MealItem.builder()
                .detailId(detail.getDetailId())
                .foodId(foodId)
                .scanId(scanId)
                .foodName(foodName)
                .quantity(detail.getQuantity())
                .calories(detail.getCalories())
                .fat(detail.getFat())
                .carbs(detail.getCarbs())
                .protein(detail.getProtein())
                .mealTime(detail.getMealTime())
                .source(detail.getSource() != null ? detail.getSource().name() : null)
                .build();
    }

}