package com.examp.genifit.repository;

import com.examp.genifit.entity.WeightProgress;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WeightProgressRepository extends JpaRepository<WeightProgress, Integer> {

    Optional<WeightProgress> findByUser_UserIdAndRecordedDate(Integer userId, LocalDate recordedDate);

    Page<WeightProgress> findAllByUser_UserId(Integer userId, Pageable pageable);

    Optional<WeightProgress> findTopByUser_UserIdOrderByRecordedDateDesc(Integer userId);
}