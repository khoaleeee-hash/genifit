package com.examp.genifit.repository;

import com.examp.genifit.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByEmailAndIsActiveTrue(String email);
    Optional<User> findByEmailAndIsActiveTrue(String email);
    Optional<User> findByUsernameAndIsActiveTrue(String username);
    List<User> findByUsernameContainingIgnoreCase(String keyword);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    
    long countByCreatedAtBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);
    long countByCreatedAtBefore(java.time.LocalDateTime end);
    List<User> findTop5ByOrderByCreatedAtDesc();
}