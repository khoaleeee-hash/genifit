package com.examp.genifit.service.serviceImpl;

import com.examp.genifit.dto.request.FoodEvaluationRequest;
import com.examp.genifit.dto.response.FoodEvaluationResponse;
import com.examp.genifit.entity.DailyLog;
import com.examp.genifit.entity.User;
import com.examp.genifit.repository.DailyLogRepository;
import com.examp.genifit.repository.GuestRepository;
import com.examp.genifit.repository.UserRepository;
import com.examp.genifit.service.FoodEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FoodEvaluationServiceImpl implements FoodEvaluationService {

    private final UserRepository userRepository;
    private final GuestRepository guestRepository;
    private final DailyLogRepository dailyLogRepository;

    @Override
    public FoodEvaluationResponse evaluateScannedFood(FoodEvaluationRequest request) {

        validateRequest(request);

        double mealCalories = safeDouble(request.getTotalCalories());
        double mealProtein = safeDouble(request.getTotalProtein());
        double mealFat = safeDouble(request.getTotalFat());
        double confidence = request.getConfidence() == null ? 0.0 : request.getConfidence();

        LocalDate today = LocalDate.now();

        double currentCaloriesToday = 0.0;
        double targetCalories = 2000.0;
        String userGoal = "UNKNOWN";

        if (request.getUserId() != null) {
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

            userGoal = getUserGoal(user);

            DailyLog dailyLog = dailyLogRepository
                    .findByUser_UserIdAndLogDate(request.getUserId(), today)
                    .orElse(null);

            if (dailyLog != null) {
                currentCaloriesToday = safeDouble(dailyLog.getTotalCalories());

                if (dailyLog.getTargetCalories() != null && dailyLog.getTargetCalories() > 0) {
                    targetCalories = dailyLog.getTargetCalories();
                }
            }

        } else {
            guestRepository.findById(request.getGuestId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy guest"));

            DailyLog dailyLog = dailyLogRepository
                    .findByGuest_GuestIdAndLogDate(request.getGuestId(), today)
                    .orElse(null);

            if (dailyLog != null) {
                currentCaloriesToday = safeDouble(dailyLog.getTotalCalories());

                if (dailyLog.getTargetCalories() != null && dailyLog.getTargetCalories() > 0) {
                    targetCalories = dailyLog.getTargetCalories();
                }
            }

            userGoal = "GUEST";
        }

        double remainingCaloriesBeforeMeal = targetCalories - currentCaloriesToday;
        double totalCaloriesAfterMeal = currentCaloriesToday + mealCalories;
        double percentOfTargetAfterMeal = totalCaloriesAfterMeal / targetCalories;

        List<String> reasons = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();

        int score = 100;

        if (confidence < 0.6) {
            score -= 15;
            reasons.add("Độ tin cậy của AI thấp, calories có thể chưa chính xác.");
            suggestions.add("Bạn nên kiểm tra lại khẩu phần hoặc nhập thủ công nếu cần chính xác hơn.");
        }

        if (mealCalories <= 0) {
            score -= 40;
            reasons.add("Không xác định được lượng calories của món ăn.");
            suggestions.add("Hãy chụp lại ảnh rõ hơn hoặc nhập tên món ăn thủ công.");
        }

        if (mealCalories > remainingCaloriesBeforeMeal) {
            score -= 35;
            reasons.add("Món ăn này vượt quá lượng calories còn lại trong ngày.");
            suggestions.add("Bạn nên giảm khẩu phần hoặc chọn món ít calories hơn.");
        }

        if (mealCalories > targetCalories * 0.5) {
            score -= 20;
            reasons.add("Món ăn chiếm hơn 50% tổng calories mục tiêu trong ngày.");
            suggestions.add("Nên chia nhỏ khẩu phần hoặc cân bằng với các bữa còn lại.");
        }

        if ("LOSE_WEIGHT".equalsIgnoreCase(userGoal)) {
            if (mealCalories > targetCalories * 0.35) {
                score -= 20;
                reasons.add("User đang có mục tiêu giảm cân nhưng món ăn có calories khá cao.");
                suggestions.add("Nên chọn món giàu protein, ít dầu mỡ và giảm tinh bột.");
            }

            if (mealFat > 30) {
                score -= 10;
                reasons.add("Lượng chất béo của món ăn khá cao so với mục tiêu giảm cân.");
                suggestions.add("Nên ưu tiên món luộc, hấp hoặc ít dầu.");
            }
        }

        if ("GAIN_WEIGHT".equalsIgnoreCase(userGoal)) {
            if (mealProtein < 15) {
                score -= 10;
                reasons.add("User đang tăng cân nhưng món ăn có lượng protein chưa cao.");
                suggestions.add("Có thể bổ sung thêm trứng, thịt gà, cá, sữa hoặc đậu.");
            }

            if (mealCalories < targetCalories * 0.2) {
                score -= 10;
                reasons.add("Món ăn hơi ít calories so với mục tiêu tăng cân.");
                suggestions.add("Nên tăng khẩu phần hoặc thêm thực phẩm giàu năng lượng lành mạnh.");
            }
        }

        if ("MAINTAIN".equalsIgnoreCase(userGoal)) {
            if (percentOfTargetAfterMeal > 1.1) {
                score -= 20;
                reasons.add("Sau khi ăn món này, tổng calories sẽ vượt mục tiêu duy trì cân nặng.");
                suggestions.add("Nên giảm khẩu phần hoặc vận động thêm trong ngày.");
            }
        }

        if (percentOfTargetAfterMeal > 1.2) {
            score -= 25;
            reasons.add("Tổng calories sau bữa ăn sẽ vượt hơn 120% mục tiêu ngày.");
            suggestions.add("Không nên ăn thêm món nhiều calories trong ngày hôm nay.");
        }

        if (reasons.isEmpty()) {
            reasons.add("Món ăn phù hợp với lượng calories và mục tiêu hiện tại.");
            suggestions.add("Bạn có thể thêm món này vào nhật ký ăn uống hôm nay.");
        }

        if (score < 0) {
            score = 0;
        }

        String suitabilityStatus = score >= 70 ? "SUITABLE" : "UNSUITABLE";

        String message = suitabilityStatus.equals("SUITABLE")
                ? "Món ăn này phù hợp với profile hiện tại của người dùng."
                : "Món ăn này chưa thật sự phù hợp với profile hiện tại của người dùng.";

        return new FoodEvaluationResponse(
                message,
                suitabilityStatus,
                score,
                reasons,
                suggestions,
                request.getFoods(),
                mealCalories,
                currentCaloriesToday,
                targetCalories,
                remainingCaloriesBeforeMeal,
                totalCaloriesAfterMeal,
                percentOfTargetAfterMeal,
                userGoal,
                confidence,
                request.getNote()
        );
    }

    private void validateRequest(FoodEvaluationRequest request) {
        if (request.getUserId() == null && request.getGuestId() == null) {
            throw new RuntimeException("Cần truyền userId hoặc guestId");
        }

        if (request.getUserId() != null && request.getGuestId() != null) {
            throw new RuntimeException("Chỉ được truyền userId hoặc guestId, không truyền cả hai");
        }

        if (request.getFoods() == null || request.getFoods().isEmpty()) {
            throw new RuntimeException("Không có dữ liệu món ăn để đánh giá");
        }
    }

    private double safeDouble(Double value) {
        return value == null ? 0.0 : value;
    }

    private String getUserGoal(User user) {
        if (user.getUserProfile() == null) {
            return "UNKNOWN";
        }

        if (user.getUserProfile().getGoal() == null) {
            return "UNKNOWN";
        }

        return user.getUserProfile().getGoal().name();
    }
}