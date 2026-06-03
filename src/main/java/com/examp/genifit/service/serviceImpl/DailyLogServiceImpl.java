package com.examp.genifit.service.serviceImpl;

import com.examp.genifit.dto.request.AddManualFoodRequest;
import com.examp.genifit.dto.response.AddManualFoodResponse;
import com.examp.genifit.entity.*;
import com.examp.genifit.repository.*;
import com.examp.genifit.service.DailyLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

    private FoodItem findFoodItem(AddManualFoodRequest request) {

        if (request.getFoodId() != null) {
            return foodItemRepository.findById(request.getFoodId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy món ăn"));
        }

        if (request.getFoodName() != null && !request.getFoodName().trim().isEmpty()) {
            return foodItemRepository.findByFoodNameIgnoreCase(request.getFoodName().trim())
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
}