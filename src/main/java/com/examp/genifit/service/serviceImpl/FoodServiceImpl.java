package com.examp.genifit.service.serviceImpl;

import com.examp.genifit.dto.request.CreateAdminFoodRequest;
import com.examp.genifit.dto.request.UpdateFoodRequest;
import com.examp.genifit.dto.response.FoodResponse;
import com.examp.genifit.entity.FoodApprovalStatus;
import com.examp.genifit.entity.FoodItem;
import com.examp.genifit.entity.User;
import com.examp.genifit.entity.UserRole;
import com.examp.genifit.repository.FoodItemRepository;
import com.examp.genifit.repository.UserRepository;
import com.examp.genifit.service.FoodService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FoodServiceImpl implements FoodService {

    private final FoodItemRepository foodItemRepository;
    private final UserRepository userRepository;

    @Override
    public Page<FoodResponse> getAllFoods(Integer pageNum, Integer pageSize) {
        Pageable pageable = buildPageable(pageNum, pageSize);

        return foodItemRepository.findByDeletedFalse(pageable)
                .map(FoodResponse::new);
    }

    @Override
    public Page<FoodResponse> searchFoods(String keyword, Integer pageNum, Integer pageSize) {
        Pageable pageable = buildPageable(pageNum, pageSize);

        if (keyword == null || keyword.trim().isEmpty()) {
            return foodItemRepository.findAll(pageable)
                    .map(FoodResponse::new);
        }

        return foodItemRepository
                .findByFoodNameContainingIgnoreCaseAndDeletedFalse(keyword.trim(), pageable)
                .map(FoodResponse::new);
    }

    @Override
    public FoodResponse createFoodByAdmin(CreateAdminFoodRequest request) {
        validateCreateFood(request);

        User admin = userRepository.findById(request.getAdminId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy admin"));

        if (admin.getRole() != UserRole.ADMIN) {
            throw new RuntimeException("Chỉ admin mới được tạo món ăn chuẩn");
        }

        foodItemRepository
                .findByFoodNameIgnoreCaseAndIsPublicTrueAndApprovalStatusAndDeletedFalse(
                        request.getFoodName().trim(),
                        FoodApprovalStatus.APPROVED
                )
                .ifPresent(food -> {
                    throw new RuntimeException("Món ăn này đã tồn tại trong danh sách món chuẩn");
                });

        FoodItem foodItem = new FoodItem();
        foodItem.setFoodName(request.getFoodName().trim());
        foodItem.setCalories(request.getCalories());
        foodItem.setProtein(request.getProtein());
        foodItem.setCarbs(request.getCarbs());
        foodItem.setFat(request.getFat());
        foodItem.setNutritionInfo(request.getNutritionInfo());

        foodItem.setCreatedBy(admin);
        foodItem.setIsPublic(true);
        foodItem.setApprovalStatus(FoodApprovalStatus.APPROVED);

        FoodItem savedFood = foodItemRepository.save(foodItem);

        return new FoodResponse(savedFood);
    }

    @Override
    public FoodResponse updateFood(Integer foodId, UpdateFoodRequest request) {
        FoodItem foodItem = foodItemRepository.findById(foodId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy món ăn"));

        validateUpdateFood(request);

        if (request.getFoodName() != null && !request.getFoodName().trim().isEmpty()) {
            foodItem.setFoodName(request.getFoodName().trim());
        }

        if (request.getCalories() != null) {
            foodItem.setCalories(request.getCalories());
        }

        if (request.getProtein() != null) {
            foodItem.setProtein(request.getProtein());
        }

        if (request.getCarbs() != null) {
            foodItem.setCarbs(request.getCarbs());
        }

        if (request.getFat() != null) {
            foodItem.setFat(request.getFat());
        }

        if (request.getNutritionInfo() != null) {
            foodItem.setNutritionInfo(request.getNutritionInfo());
        }

        if (request.getIsPublic() != null) {
            foodItem.setIsPublic(request.getIsPublic());
        }

        if (Boolean.TRUE.equals(foodItem.getIsPublic())) {
            foodItem.setApprovalStatus(FoodApprovalStatus.APPROVED);
        }

        FoodItem updatedFood = foodItemRepository.save(foodItem);

        return new FoodResponse(updatedFood);
    }

    @Override
    public void softDeleteFood(Integer foodId) {
        FoodItem foodItem = foodItemRepository.findById(foodId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy món ăn"));

        if(Boolean.TRUE.equals(foodItem.getDeleted())){
            throw new RuntimeException("Món ăn này đã được xoá trước đó");
        }

        foodItem.setDeleted(true);
        foodItem.setIsPublic(false);

        foodItemRepository.save(foodItem);
    }

    private void validateCreateFood(CreateAdminFoodRequest request) {
        if (request.getAdminId() == null) {
            throw new RuntimeException("AdminId không được để trống");
        }

        if (request.getFoodName() == null || request.getFoodName().trim().isEmpty()) {
            throw new RuntimeException("Tên món ăn không được để trống");
        }

        if (request.getCalories() == null || request.getCalories() < 0) {
            throw new RuntimeException("Calories không hợp lệ");
        }

        if (request.getProtein() != null && request.getProtein() < 0) {
            throw new RuntimeException("Protein không hợp lệ");
        }

        if (request.getCarbs() != null && request.getCarbs() < 0) {
            throw new RuntimeException("Carbs không hợp lệ");
        }

        if (request.getFat() != null && request.getFat() < 0) {
            throw new RuntimeException("Fat không hợp lệ");
        }
    }

    private void validateUpdateFood(UpdateFoodRequest request) {
        if (request.getCalories() != null && request.getCalories() < 0) {
            throw new RuntimeException("Calories không hợp lệ");
        }

        if (request.getProtein() != null && request.getProtein() < 0) {
            throw new RuntimeException("Protein không hợp lệ");
        }

        if (request.getCarbs() != null && request.getCarbs() < 0) {
            throw new RuntimeException("Carbs không hợp lệ");
        }

        if (request.getFat() != null && request.getFat() < 0) {
            throw new RuntimeException("Fat không hợp lệ");
        }
    }

    private Pageable buildPageable(Integer pageNum, Integer pageSize) {
        if (pageNum == null || pageNum < 0) {
            pageNum = 0;
        }

        if (pageSize == null || pageSize <= 0) {
            pageSize = 10;
        }

        if (pageSize > 50) {
            pageSize = 50;
        }

        return PageRequest.of(
                pageNum,
                pageSize,
                Sort.by(Sort.Direction.ASC, "foodId")
        );
    }
}