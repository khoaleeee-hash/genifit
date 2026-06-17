package com.examp.genifit.repository;

import com.examp.genifit.entity.FoodApprovalStatus;
import com.examp.genifit.entity.FoodItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FoodItemRepository extends JpaRepository<FoodItem, Integer> {

    Page<FoodItem> findByDeletedFalse(Pageable pageable);

    Page<FoodItem> findByFoodNameContainingIgnoreCaseAndDeletedFalse(
            String keyword,
            Pageable pageable
    );

    Optional<FoodItem> findByFoodIdAndDeletedFalse(
            Integer foodId
    );

    Optional<FoodItem> findByFoodNameIgnoreCaseAndDeletedFalse(
            String foodName
    );

    Optional<FoodItem> findByFoodNameIgnoreCaseAndIsPublicTrueAndApprovalStatusAndDeletedFalse(
            String foodName,
            FoodApprovalStatus approvalStatus
    );

    @Query("""
        SELECT f
        FROM FoodItem f
        WHERE (:foodId IS NULL OR f.foodId = :foodId)
        AND (:keyword IS NULL OR LOWER(f.foodName) LIKE LOWER(CONCAT('%', :keyword, '%')))
        AND (:calories IS NULL OR f.calories = :calories)
        AND (:caloriesFrom IS NULL OR f.calories >= :caloriesFrom)
        AND (:caloriesTo IS NULL OR f.calories <= :caloriesTo)
        AND (:proteinFrom IS NULL OR f.protein >= :proteinFrom)
        AND (:proteinTo IS NULL OR f.protein <= :proteinTo)
        AND (:carbsFrom IS NULL OR f.carbs >= :carbsFrom)
        AND (:carbsTo IS NULL OR f.carbs <= :carbsTo)
        AND (:fatFrom IS NULL OR f.fat >= :fatFrom)
        AND (:fatTo IS NULL OR f.fat <= :fatTo)
        AND (:isPublic IS NULL OR f.isPublic = :isPublic)
        AND f.deleted = :isDeleted
        """)
    Page<FoodItem> filterFoods(
            @Param("foodId") Integer foodId,
            @Param("keyword") String keyword,
            @Param("calories") Double calories,
            @Param("caloriesFrom") Double caloriesFrom,
            @Param("caloriesTo") Double caloriesTo,
            @Param("proteinFrom") Double proteinFrom,
            @Param("proteinTo") Double proteinTo,
            @Param("carbsFrom") Double carbsFrom,
            @Param("carbsTo") Double carbsTo,
            @Param("fatFrom") Double fatFrom,
            @Param("fatTo") Double fatTo,
            @Param("isPublic") Boolean isPublic,
            @Param("isDeleted") Boolean isDeleted,
            Pageable pageable
    );

    List<FoodItem> findByIsPublicTrueAndApprovalStatusAndDeletedFalse(
            FoodApprovalStatus approvalStatus
    );

    List<FoodItem> findByCreatedBy_UserIdAndDeletedFalse(
            Integer userId
    );
}