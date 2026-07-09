package com.examp.genifit.repository;

import com.examp.genifit.entity.PaymentTransaction;
import com.examp.genifit.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Integer> {
    Optional<PaymentTransaction> findByOrderCode(String orderCode);

    // Lần đầu load — lấy N giao dịch mới nhất
    List<PaymentTransaction> findByUserOrderByTransactionIdDesc(User user, Pageable pageable);

    // Load thêm — lấy N giao dịch cũ hơn cursor
    List<PaymentTransaction> findByUserAndTransactionIdLessThanOrderByTransactionIdDesc(
            User user, Integer cursorId, Pageable pageable);
}
