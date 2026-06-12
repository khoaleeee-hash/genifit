package com.examp.genifit.repository;

import com.examp.genifit.entity.FoodApprovalStatus;
import com.examp.genifit.entity.FoodItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FoodItemRepository extends JpaRepository<FoodItem, Integer> {

    List<FoodItem> findByFoodNameContainingIgnoreCase(String keyword);
    Optional<FoodItem> findByFoodNameIgnoreCase(String foodName);
    Optional<FoodItem> findByFoodNameIgnoreCaseAndIsPublicTrueAndApprovalStatus(String foodName, FoodApprovalStatus approvalStatus);
    List<FoodItem> findByIsPublicTrueAndApprovalStatus(FoodApprovalStatus approvalStatus);
    List<FoodItem> findByCreatedBy_UserId(Integer userId);
}