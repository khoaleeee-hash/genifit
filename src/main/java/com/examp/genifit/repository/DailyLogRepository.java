package com.examp.genifit.repository;

import com.examp.genifit.entity.DailyLog;
import com.examp.genifit.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyLogRepository extends JpaRepository<DailyLog, Integer> {
    Optional<DailyLog> findByUserAndLogDate(User user, LocalDate date);
    List<DailyLog> findByUserAndLogDateBetween(User user, LocalDate startDate, LocalDate endDate);
}
