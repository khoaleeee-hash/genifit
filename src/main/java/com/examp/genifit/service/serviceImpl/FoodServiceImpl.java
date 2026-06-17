package com.examp.genifit.service.serviceImpl;

import com.examp.genifit.dto.request.CreateAdminFoodRequest;
import com.examp.genifit.dto.request.FoodFilterRequest;
import com.examp.genifit.dto.request.UpdateFoodRequest;
import com.examp.genifit.dto.response.FoodResponse;
import com.examp.genifit.dto.response.PageInfoResponse;
import com.examp.genifit.dto.response.PageResponse;
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

//    @Override
//    public PageResponse<FoodResponse> getAllFoods(int pageNum, int pageSize) {
//        Pageable pageable = buildPageable(pageNum, pageSize);
//
//        Page<FoodResponse> foodPage = foodItemRepository.findByDeletedFalse(pageable)
//                .map(FoodResponse::new);
//
//        return buildPageResponse(foodPage);
//    }

//    @Override
//    public PageResponse<FoodResponse> searchFoods(String keyword, int pageNum, int pageSize) {
//        Pageable pageable = buildPageable(pageNum, pageSize);
//
//        Page<FoodResponse> foodPage;
//
//        if (keyword == null || keyword.trim().isEmpty()) {
//            foodPage = foodItemRepository.findByDeletedFalse(pageable)
//                    .map(FoodResponse::new);
//        } else {
//            foodPage = foodItemRepository
//                    .findByFoodNameContainingIgnoreCaseAndDeletedFalse(keyword.trim(), pageable)
//                    .map(FoodResponse::new);
//        }
//
//        return buildPageResponse(foodPage);
//    }

    @Override
    public PageResponse<FoodResponse> filterFoods(FoodFilterRequest request) {
        int pageNum = 1;
        int pageSize = 10;

        FoodFilterRequest.SearchCondition condition = null;

        if (request != null) {
            condition = request.getSearchCondition();

            if (request.getPageInfo() != null) {
                if (request.getPageInfo().getPageNum() != null) {
                    pageNum = request.getPageInfo().getPageNum();
                }

                if (request.getPageInfo().getPageSize() != null) {
                    pageSize = request.getPageInfo().getPageSize();
                }
            }
        }

        Pageable pageable = buildPageable(pageNum, pageSize);

        Integer foodId = null;
        String keyword = null;

        Double calories = null;
        Double caloriesFrom = null;
        Double caloriesTo = null;

        Double proteinFrom = null;
        Double proteinTo = null;
        Double carbsFrom = null;
        Double carbsTo = null;
        Double fatFrom = null;
        Double fatTo = null;

        Boolean isPublic = null;
        Boolean isDeleted = false;

        if (condition != null) {
            foodId = condition.getFoodId();

            if (condition.getKeyword() != null && !condition.getKeyword().trim().isEmpty()) {
                keyword = condition.getKeyword().trim();
            }

            calories = condition.getCalories();
            caloriesFrom = condition.getCaloriesFrom();
            caloriesTo = condition.getCaloriesTo();

            proteinFrom = condition.getProteinFrom();
            proteinTo = condition.getProteinTo();

            carbsFrom = condition.getCarbsFrom();
            carbsTo = condition.getCarbsTo();

            fatFrom = condition.getFatFrom();
            fatTo = condition.getFatTo();

            isPublic = condition.getIsPublic();

            if (condition.getIsDeleted() != null) {
                isDeleted = condition.getIsDeleted();
            }
        }

        Page<FoodResponse> foodPage = foodItemRepository
                .filterFoods(
                        foodId,
                        keyword,
                        calories,
                        caloriesFrom,
                        caloriesTo,
                        proteinFrom,
                        proteinTo,
                        carbsFrom,
                        carbsTo,
                        fatFrom,
                        fatTo,
                        isPublic,
                        isDeleted,
                        pageable
                )
                .map(FoodResponse::new);

        return buildPageResponse(foodPage);
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

        FoodItem foodItem = foodItemRepository.findByFoodIdAndDeletedFalse(foodId)
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

        if (Boolean.TRUE.equals(foodItem.getDeleted())) {
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

    private Pageable buildPageable(int pageNum, int pageSize) {
        if (pageNum <= 0) {
            pageNum = 1;
        }

        if (pageSize <= 0) {
            pageSize = 10;
        }

        if (pageSize > 50) {
            pageSize = 50;
        }

        int springPageIndex = pageNum - 1;

        return PageRequest.of(
                springPageIndex,
                pageSize,
                Sort.by(Sort.Direction.ASC, "foodId")
        );
    }

//    private PageResponse<FoodResponse> buildPageResponse(Page<FoodResponse> page) {
//        PageInfoResponse pageInfo = new PageInfoResponse(
//                page.getNumber() + 1,
//                page.getSize(),
//                page.getTotalPages(),
//                page.getTotalElements()
//        );
//
//        return new PageResponse<>(
//                page.getContent(),
//                pageInfo
//        );
//    }
    private PageResponse<FoodResponse> buildPageResponse(Page<FoodResponse> page) {
        PageInfoResponse pageInfo = PageInfoResponse.builder()
                .pageNum(page.getNumber() + 1)
                .pageSize(page.getSize())
                .totalPage(page.getTotalPages())
                .totalItem(page.getTotalElements())
                .build();

        return new PageResponse<>(
                page.getContent(),
                pageInfo
        );
}
}