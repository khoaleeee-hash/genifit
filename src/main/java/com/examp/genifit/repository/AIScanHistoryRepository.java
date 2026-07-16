package com.examp.genifit.repository;

import com.examp.genifit.entity.AIScanHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AIScanHistoryRepository extends JpaRepository<AIScanHistory, Integer> {
    List<AIScanHistory> findByUser_UserIdOrderByCreatedAtDesc(Integer userId);
    List<AIScanHistory> findByGuest_GuestIdOrderByCreatedAtDesc(Integer guestId);

    long countByUser_UserIdAndCreatedAtGreaterThanEqual(Integer userId, LocalDateTime startOfDay);
    long countByGuest_GuestIdAndCreatedAtGreaterThanEqual(Integer guestId, LocalDateTime startOfDay);
}