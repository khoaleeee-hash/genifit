package com.examp.genifit.repository;

import com.examp.genifit.entity.OtpToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface OtpTokenRepository extends JpaRepository<OtpToken, Integer> {
    Optional<OtpToken> findByEmailAndOtpCode(String email, String otpCode);
    void deleteByEmail(String email);
}