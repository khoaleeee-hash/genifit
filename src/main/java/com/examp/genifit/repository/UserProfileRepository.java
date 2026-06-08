package com.examp.genifit.repository;

import com.examp.genifit.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Integer> {

    Optional<UserProfile> findByUser_UserId(Integer userId);

}