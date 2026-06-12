package com.examp.genifit.repository;

import com.examp.genifit.entity.MealReminder;
import com.examp.genifit.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MealReminderRepository extends JpaRepository<MealReminder, Integer> {
    List<MealReminder> findByUserOrderByReminderTimeAsc(User user);
    boolean existsByUserAndReminderTime(User user, java.time.LocalTime reminderTime);
}
