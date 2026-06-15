package com.examp.genifit.service.prompt;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FoodRecommendationPrompt {

    public String build(
            String userName,
            double remainingCalories,
            String mealType,           // "bữa sáng", "bữa trưa", "bữa tối", "bữa phụ"
            double weightKg,
            double heightCm,
            String goal,               // LOSE_WEIGHT, GAIN_WEIGHT, MAINTAIN
            List<String> medicalConditions,
            List<String> allergies
    ) {
        String conditionsText = medicalConditions.isEmpty()
                ? "Không có"
                : String.join(", ", medicalConditions);

        String allergiesText = allergies.isEmpty()
                ? "Không có"
                : String.join(", ", allergies);

        return """
                Bạn là GENEFIT AI — chuyên gia dinh dưỡng cá nhân.
                
                ## THÔNG TIN NGƯỜI DÙNG
                - Tên: %s
                - Cân nặng: %.1f kg | Chiều cao: %.1f cm
                - Mục tiêu: %s
                - Bệnh nền: %s
                - Dị ứng thực phẩm: %s
                
                ## YÊU CẦU
                - Gợi ý 3 món ăn phù hợp cho %s
                - Lượng calo còn lại trong ngày: %.0f kcal
                - Mỗi món phải phù hợp với bệnh nền và TUYỆT ĐỐI không chứa thành phần gây dị ứng
                
                ## ĐỊNH DẠNG TRẢ LỜI (JSON, không thêm bất kỳ text nào khác)
                [
                  {
                    "name": "Tên món",
                    "calories": 350,
                    "description": "Mô tả ngắn tại sao phù hợp",
                    "ingredients": ["nguyên liệu 1", "nguyên liệu 2"],
                    "warning": "Lưu ý đặc biệt nếu có (hoặc null)"
                  }
                ]
                """.formatted(
                userName, weightKg, heightCm, goal,
                conditionsText, allergiesText,
                mealType, remainingCalories
        );
    }
}