package com.examp.genifit.service.serviceImpl;

import com.examp.genifit.dto.request.CreateFoodRequest;
import com.examp.genifit.dto.response.FoodResponse;
import com.examp.genifit.entity.FoodItem;
import com.examp.genifit.repository.FoodItemRepository;
import com.examp.genifit.service.FoodService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FoodServiceImpl implements FoodService {
    private final FoodItemRepository foodItemRepository;

    public List<FoodResponse> getAllFoods(){
        return foodItemRepository.findAll()
                .stream()
                .map(FoodResponse::new)
                .toList();
    }

    public List<FoodResponse> searchFoods(String keyword){
        if(keyword == null || keyword.trim().isEmpty()){
            return getAllFoods();
        }
        return foodItemRepository.findByFoodNameContainingIgnoreCase(keyword.trim())
                .stream()
                .map(FoodResponse::new)
                .toList();
    }

    public FoodResponse createFood(CreateFoodRequest request){
        if(request.getFoodName() == null || request.getFoodName().trim().isEmpty()){
            throw new RuntimeException("Tên món ăn không được để trống");
        }
        if(request.getCalories() == null || request.getCalories() < 0) {
            throw new RuntimeException("Calories không hợp lệ");
        }
        foodItemRepository.findByFoodNameIgnoreCase(request.getFoodName().trim())
                .ifPresent(food -> {
                    throw new RuntimeException("Món ăn đã tồn tại: " + request.getFoodName());
                });
        FoodItem foodItem = new FoodItem();
        foodItem.setFoodName(request.getFoodName().trim());
        foodItem.setCalories(request.getCalories());
        foodItem.setProtein(request.getProtein());
        foodItem.setCarbs(request.getCarbs());
        foodItem.setFat(request.getFat());
        foodItem.setNutritionInfo(request.getNutritionInfo());

        FoodItem savedFood = foodItemRepository.save(foodItem);

        return new FoodResponse(savedFood);
    }
}
