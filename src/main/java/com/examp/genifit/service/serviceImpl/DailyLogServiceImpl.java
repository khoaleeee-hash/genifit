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

    public AddManualFoodResponse addManualFood(AddManualFoodRequest request){
        if (request.getUserId() == null && request.getGuestId() == null) {
            throw new RuntimeException("Cần truyền userId hoặc guestId");
        }
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new RuntimeException("Số lượng món ăn phải lớn 0");
        }
        FoodItem foodItem = findFoodItem(request);
        LocalDate today = LocalDate.now();
        DailyLog dailyLog;

        if(request.getUserId() != null){
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));
            dailyLog = dailyLogRepository
                    .findByUser_UserIdAndLogDate(request.getUserId(), today)
                    .orElseGet(() -> createDailyLogForUser(user));
        }else {
            Guest guest = guestRepository.findById(request.getGuestId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy guest"));
            dailyLog = dailyLogRepository
                    .findByGuest_GuestIdAndLogDate(request.getGuestId(), today)
                    .orElseGet(() -> createDailyLogForGuest(guest));

        }

        double addCalories  = foodItem.getCalories() * request.getQuantity();

        LogDetail logDetail = new LogDetail();
        logDetail.setDailyLog(dailyLog);
        logDetail.setFoodItem(foodItem);
        logDetail.setQuantity(request.getQuantity());
        logDetail.setCalories(addCalories);
        logDetail.setSource(FoodSource.MANUAL);
        logDetail.setMealTime(
                request.getMealTime() == null ? MealTime.SNACK : request.getMealTime()
        );
        logDetail.setCreatedAt(LocalDateTime.now());
        logDetailRepository.save(logDetail);

        double currentTotal = dailyLog.getTotalCalories() == null ? 0 : dailyLog.getTotalCalories();
        double newTotal = currentTotal + addCalories;

        dailyLog.setTotalCalories(newTotal);
        dailyLog.setStatusColor(calculateStatusColor(newTotal, dailyLog.getTargetCalories()));
        dailyLogRepository.save(dailyLog);

        return new AddManualFoodResponse(
                "Thêm món ăn thành công",
                foodItem.getFoodName(),
                request.getQuantity(),
                addCalories,
                newTotal,
                dailyLog.getStatusColor().name()
        );
    }

    private FoodItem findFoodItem(AddManualFoodRequest request){
        if(request.getFoodId() != null){
            return foodItemRepository.findById(request.getFoodId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy món ăn"));
        }
        if(request.getFoodName() != null && !request.getFoodName().trim().isEmpty()){
            return foodItemRepository.findByFoodNameIgnoreCase(request.getFoodName().trim())
                    .orElseThrow(()-> new RuntimeException("Không tìm thấy món ăn có tên: " + request.getFoodName()));

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


    private StatusColor calculateStatusColor(Double totalCalories, Double tagretCalories){
        if(tagretCalories == null || tagretCalories <= 0){
            tagretCalories = 2000.0;
        }

        double percent = totalCalories / tagretCalories;

        if(percent < 0.9) {
            return StatusColor.BLUE;
        } else if (percent <= 1.05){
            return StatusColor.GREEN;
        } else if (percent <= 1.2){
            return StatusColor.YELLOW;
        } else {
            return StatusColor.RED;
        }
    }
}
