package com.examp.genifit.repository;

import com.examp.genifit.entity.LogDetail;
import com.examp.genifit.entity.MealTime;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogDetailRepository extends JpaRepository<LogDetail, Integer> {
    boolean existsByDailyLog_LogIdAndFoodItem_FoodIdAndMealTime(
            Integer logId,
            Integer foodId,
            MealTime mealTime
    );
}
