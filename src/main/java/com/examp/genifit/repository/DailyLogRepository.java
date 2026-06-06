package com.examp.genifit.repository;

import com.examp.genifit.dto.response.HomeStatusResponse;
import com.examp.genifit.entity.DailyLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyLogRepository extends JpaRepository<DailyLog, Integer> {
    Optional<DailyLog> findByUser_UserIdAndLogDate(Integer userId, LocalDate logDate);
    Optional<DailyLog> findByGuest_GuestIdAndLogDate(Integer guestId, LocalDate logDate);
    List<DailyLog> findAllByUser_UserIdAndLogDateBetweenOrderByLogDateAsc(Integer userId, LocalDate startDate, LocalDate endDate);
}
