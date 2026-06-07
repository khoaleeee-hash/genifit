package com.examp.genifit.repository;

import com.examp.genifit.entity.AdvancedProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdvancedProfileRepository extends JpaRepository<AdvancedProfile, Integer> {

    Optional<AdvancedProfile> findByUser_UserId(Integer userId);

}