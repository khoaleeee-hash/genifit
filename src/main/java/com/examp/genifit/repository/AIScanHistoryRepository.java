package com.examp.genifit.repository;

import com.examp.genifit.entity.AIScanHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AIScanHistoryRepository extends JpaRepository<AIScanHistory, Integer> {
    List<AIScanHistory> findByUser_UserIdOrderByCreatedAtDesc(Integer userId);
    List<AIScanHistory> findByGuest_GuestIdOrderByCreatedAtDesc(Integer guestId);
}