package com.examp.genifit.repository;

import com.examp.genifit.entity.PaymentTransaction;
import com.examp.genifit.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Integer> {
    Optional<PaymentTransaction> findByOrderCode(String orderCode);

    // Lần đầu load — lấy N giao dịch mới nhất
    List<PaymentTransaction> findByUserOrderByTransactionIdDesc(User user, Pageable pageable);

    // Load thêm — lấy N giao dịch cũ hơn cursor
    List<PaymentTransaction> findByUserAndTransactionIdLessThanOrderByTransactionIdDesc(
            User user, Integer cursorId, Pageable pageable);

    List<PaymentTransaction> findAllByOrderByTransactionIdDesc(Pageable pageable);

    List<PaymentTransaction> findByTransactionIdLessThanOrderByTransactionIdDesc(
            Integer cursorId, Pageable pageable);

    @Query("SELECT SUM(p.amount) FROM PaymentTransaction p WHERE p.status = :status AND p.createdAt BETWEEN :start AND :end")
    BigDecimal sumAmountByStatusAndCreatedAtBetween(
            @Param("status") PaymentTransaction.PaymentStatus status, 
            @Param("start") LocalDateTime start, 
            @Param("end") LocalDateTime end);

    @Query("SELECT SUM(p.amount) FROM PaymentTransaction p WHERE p.status = :status AND p.createdAt < :end")
    BigDecimal sumAmountByStatusAndCreatedAtBefore(
            @Param("status") PaymentTransaction.PaymentStatus status, 
            @Param("end") LocalDateTime end);

    List<PaymentTransaction> findTop5ByStatusOrderByCreatedAtDesc(PaymentTransaction.PaymentStatus status);
}
