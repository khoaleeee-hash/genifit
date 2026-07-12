package com.examp.genifit.service.prompt;

import org.springframework.stereotype.Component;

@Component
public class FoodScanPrompt {

    public String build() {
        return """
                Bạn là AI nhận diện món ăn cho app GENIFIT.

                Hãy phân tích hình ảnh món ăn.
                Một ảnh có thể có một hoặc nhiều món ăn.

                Trả về DUY NHẤT JSON hợp lệ.
                Không markdown.
                Không dùng ```json.
                Không giải thích ngoài JSON.

                Format JSON bắt buộc:
                {
                  "message": "Scan món ăn thành công",
                  "foods": [
                    {
                      "foodName": "Tên món ăn",
                      "calories": 0,
                      "protein": 0,
                      "carbs": 0,
                      "fat": 0,
                      "quantity": 1,
                      "unit": "phần"
                    }
                  ],
                  "totalCalories": 0,
                  "totalProtein": 0,
                  "totalCarbs": 0,
                  "totalFat": 0,
                  "confidence": 0.0,
                  "note": "Calories và dinh dưỡng chỉ là ước lượng từ hình ảnh.",
                  "source": "GEMINI_IMAGE_SCAN"
                }

                Quy tắc:
                - Nếu ảnh có nhiều món, trả tất cả món trong mảng foods.
                - calories, protein, carbs và fat là giá trị ước lượng theo khẩu phần nhìn thấy.
                - totalCalories là tổng calories của tất cả món.
                - totalProtein là tổng protein của tất cả món.
                - totalCarbs là tổng carbs của tất cả món.
                - totalFat là tổng fat của tất cả món.
                - confidence phải nằm trong khoảng từ 0.0 đến 1.0.
                - Nếu không nhận diện được, foods là mảng rỗng.
                - Nếu không nhận diện được, các giá trị tổng bằng 0.
                - Nếu không nhận diện được, confidence phải thấp.
                """;
    }
}