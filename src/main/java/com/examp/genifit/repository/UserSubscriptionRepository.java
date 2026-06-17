package com.examp.genifit.repository;

import com.examp.genifit.entity.SubscriptionStatus;
import com.examp.genifit.entity.User;
import com.examp.genifit.entity.UserSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Integer> {
    Optional<UserSubscription> findFirstByUserAndStatusOrderByEndDateDesc(
            User user,
            SubscriptionStatus status
    );
    List<UserSubscription> findByUserOrderByCreatedAtDesc(User user);
    Optional<UserSubscription> findByUserUserIdAndStatus(Integer userId, SubscriptionStatus status);
    boolean existsByUserAndStatus(User user, SubscriptionStatus status);
}
