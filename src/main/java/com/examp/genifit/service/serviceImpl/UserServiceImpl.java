package com.examp.genifit.service.serviceImpl;

import com.examp.genifit.common.exception.ApiException;
import com.examp.genifit.common.exception.ErrorCode;
import com.examp.genifit.dto.request.CreateUserRequest;
import com.examp.genifit.dto.response.UserResponse;
import com.examp.genifit.entity.OtpToken;
import com.examp.genifit.entity.User;
import com.examp.genifit.entity.UserRole;
import com.examp.genifit.mapper.UserMapper;
import com.examp.genifit.repository.OtpTokenRepository;
import com.examp.genifit.repository.UserRepository;
import com.examp.genifit.service.EmailService;
import com.examp.genifit.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements UserService {
    UserRepository userRepository;
    OtpTokenRepository otpTokenRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    EmailService emailService;

    @Override
    @Transactional
    public void generateAndSendOtp(String email) {
        if (userRepository.existsByUsername(email)) {
            throw new ApiException(ErrorCode.USER_EXISTED);
        }

        otpTokenRepository.deleteByEmail(email);

        String otp = String.format("%06d", new Random().nextInt(999999));

        OtpToken otpToken = OtpToken.builder()
                .email(email)
                .otpCode(otp)
                .expiryTime(LocalDateTime.now().plusMinutes(5))
                .build();
        otpTokenRepository.save(otpToken);

        emailService.sendOtpEmail(email, otp);
    }

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ApiException(ErrorCode.USER_EXISTED);
        }

        OtpToken validOtp = otpTokenRepository.findByEmailAndOtpCode(request.getUsername(), request.getOtpCode())
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_OTP));

        if (validOtp.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new ApiException(ErrorCode.OTP_EXPIRED);
        }

        User user = userMapper.toUser(request);
        user.setPasswordHash(passwordEncoder.encode(request.getPasswordHash()));
        user.setRole(UserRole.MEMBER);
        userRepository.save(user);

        otpTokenRepository.delete(validOtp);

        return userMapper.toUserResponse(user);
    }

    @Override
    public void deleteUserById(Integer id) {}

    @Override
    public UserResponse getUser(Integer id) { return null; }

    @Override
    public List<UserResponse> getUsers() { return List.of(); }
}