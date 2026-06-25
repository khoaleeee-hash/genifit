package com.examp.genifit.service.serviceImpl;

import com.examp.genifit.common.exception.ApiException;
import com.examp.genifit.common.exception.ErrorCode;
import com.examp.genifit.dto.request.ChangePasswordRequest;
import com.examp.genifit.dto.request.AssignSubscriptionRequest;
import com.examp.genifit.dto.request.CreateUserRequest;
import com.examp.genifit.dto.request.ResetPasswordRequest;
import com.examp.genifit.dto.request.UpdateUserProfileRequest;
import com.examp.genifit.dto.response.UserProfileResponse;
import com.examp.genifit.dto.response.UserResponse;
import com.examp.genifit.entity.OtpToken;
import com.examp.genifit.entity.User;
import com.examp.genifit.entity.UserProfile;
import com.examp.genifit.entity.UserRole;
import com.examp.genifit.mapper.UserMapper;
import com.examp.genifit.repository.OtpTokenRepository;
import com.examp.genifit.repository.UserProfileRepository;
import com.examp.genifit.dto.response.UserSubscriptionResponse;
import com.examp.genifit.entity.*;
import com.examp.genifit.mapper.UserMapper;
import com.examp.genifit.repository.OtpTokenRepository;
import com.examp.genifit.repository.SubscriptionPlanRepository;
import com.examp.genifit.repository.UserRepository;
import com.examp.genifit.repository.UserSubscriptionRepository;
import com.examp.genifit.service.EmailService;
import com.examp.genifit.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    private final UserSubscriptionRepository userSubscriptionRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;

    UserRepository userRepository;
    OtpTokenRepository otpTokenRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    EmailService emailService;
    UserProfileRepository userProfileRepository;

    @Override
    @Transactional
    public void generateAndSendOtp(String email) {
        if (userRepository.existsByEmail(email)) {
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

        emailService.sendRegistrationOtpEmail(email, otp);
    }

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ApiException(ErrorCode.USER_EXISTED);
        }

        OtpToken validOtp = otpTokenRepository.findByEmailAndOtpCode(request.getEmail(), request.getOtpCode())
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_OTP));

        if (validOtp.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new ApiException(ErrorCode.OTP_EXPIRED);
        }

        User user = userMapper.toUser(request);
        user.setPasswordHash(passwordEncoder.encode(request.getPasswordHash()));
        user.setRole(UserRole.MEMBER);
        user.setIsActive(true);
        userRepository.save(user);

        otpTokenRepository.delete(validOtp);

        return userMapper.toUserResponse(user);
    }

    @Override
    public UserResponse getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String currentUsername = context.getAuthentication().getName();

        User user = userRepository.findByUsernameAndIsActiveTrue(currentUsername)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        return userMapper.toUserResponse(user);
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        var context = SecurityContextHolder.getContext();
        String currentUsername = context.getAuthentication().getName();

        User user = userRepository.findByUsernameAndIsActiveTrue(currentUsername)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR);
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
    public UserSubscriptionResponse assignSubscription(AssignSubscriptionRequest request) {
        if (request == null || request.getUserId() == null || request.getPlanId() == null) {
            throw new ApiException(ErrorCode.INVALID_SUBSCRIPTION_REQUEST);
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        SubscriptionPlan plan = subscriptionPlanRepository.findById(request.getPlanId())
                .orElseThrow(() -> new ApiException(ErrorCode.SUBSCRIPTION_PLAN_NOT_FOUND));

        if (plan.getActive() == null || !plan.getActive()) {
            throw new ApiException(ErrorCode.SUBSCRIPTION_PLAN_INACTIVE);
        }

        userSubscriptionRepository
                .findFirstByUserAndStatusOrderByEndDateDesc(user, SubscriptionStatus.ACTIVE)
                .ifPresent(oldSubscription -> {
                    oldSubscription.setStatus(SubscriptionStatus.CANCELLED);
                    oldSubscription.setCancelledAt(LocalDateTime.now());
                    userSubscriptionRepository.save(oldSubscription);
                });

        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusDays(plan.getDurationDays());

        UserSubscription subscription = UserSubscription.builder()
                .user(user)
                .subscriptionPlan(plan)
                .startDate(startDate)
                .endDate(endDate)
                .status(SubscriptionStatus.ACTIVE)
                .autoRenew(request.getAutoRenew() == null ? false : request.getAutoRenew())
                .build();

        UserSubscription savedSubscription = userSubscriptionRepository.save(subscription);

        return new UserSubscriptionResponse(savedSubscription);
    }

    @Override
    public UserSubscriptionResponse getMyActiveSubscription() {
        User currentUser = getCurrentUser();

        UserSubscription subscription = userSubscriptionRepository
                .findFirstByUserAndStatusOrderByEndDateDesc(
                        currentUser,
                        SubscriptionStatus.ACTIVE
                )
                .orElseThrow(() -> new ApiException(ErrorCode.ACTIVE_SUBSCRIPTION_NOT_FOUND));

        if (subscription.getEndDate().isBefore(LocalDateTime.now())) {
            subscription.setStatus(SubscriptionStatus.EXPIRED);
            userSubscriptionRepository.save(subscription);

            throw new ApiException(ErrorCode.ACTIVE_SUBSCRIPTION_NOT_FOUND);
        }

        return new UserSubscriptionResponse(subscription);
    }

    @Override
    public List<UserSubscriptionResponse> getMySubscriptionHistory() {
        User currentUser = getCurrentUser();

        return userSubscriptionRepository.findByUserOrderByCreatedAtDesc(currentUser)
                .stream()
                .map(UserSubscriptionResponse::new)
                .toList();
    }

    @Override
    public void cancelMySubscription() {
        User currentUser = getCurrentUser();

        UserSubscription subscription = userSubscriptionRepository
                .findFirstByUserAndStatusOrderByEndDateDesc(
                        currentUser,
                        SubscriptionStatus.ACTIVE
                )
                .orElseThrow(() -> new ApiException(ErrorCode.ACTIVE_SUBSCRIPTION_NOT_FOUND));

        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscription.setCancelledAt(LocalDateTime.now());

        userSubscriptionRepository.save(subscription);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ApiException(ErrorCode.UNAUTHENTICATED);
        }

        Object principal = authentication.getPrincipal();

        if (principal == null || principal.equals("anonymousUser")) {
            throw new ApiException(ErrorCode.UNAUTHENTICATED);
        }

        String username = authentication.getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
    }

    @Override
    public void deleteUserById(Integer id) {}

    @Override
    @Transactional
    public void deleteMe() {
        var context = SecurityContextHolder.getContext();
        String currentUsername = context.getAuthentication().getName();

        User user = userRepository.findByUsernameAndIsActiveTrue(currentUsername)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        user.setIsActive(false);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void generateAndSendOtpForForgotPassword(String email) {
        if (!userRepository.existsByEmailAndIsActiveTrue(email)) {
            throw new ApiException(ErrorCode.USER_NOT_FOUND);
        }

        otpTokenRepository.deleteByEmail(email);
        String otp = String.format("%06d", new Random().nextInt(999999));

        OtpToken otpToken = OtpToken.builder()
                .email(email)
                .otpCode(otp)
                .expiryTime(LocalDateTime.now().plusMinutes(5))
                .build();
        otpTokenRepository.save(otpToken);

        emailService.sendForgotPasswordOtpEmail(email, otp);
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByEmailAndIsActiveTrue(request.getEmail())
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        OtpToken validOtp = otpTokenRepository.findByEmailAndOtpCode(request.getEmail(), request.getOtpCode())
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_OTP));

        if (validOtp.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new ApiException(ErrorCode.OTP_EXPIRED);
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        otpTokenRepository.delete(validOtp);
    }

    @Override
    public List<UserResponse> searchUsers(String keyword) {
        List<User> users = userRepository.findByUsernameContainingIgnoreCase(keyword);
        return users.stream().map(userMapper::toUserResponse).toList();
    }

    @Override
    public UserResponse getUser(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        return userMapper.toUserResponse(user);
    }

    @Override
    public List<UserResponse> getUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toUserResponse).toList();
    }

    @Override
    @Transactional
    public void deleteUserById(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        user.setIsActive(false);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public UserProfileResponse updateMyProfile(UpdateUserProfileRequest request) {
        var context = SecurityContextHolder.getContext();
        String currentUsername = context.getAuthentication().getName();

        User user = userRepository.findByUsernameAndIsActiveTrue(currentUsername)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        UserProfile profile = userProfileRepository.findByUser(user)
                .orElse(new UserProfile());

        profile.setUser(user);
        profile.setHeightCm(request.getHeightCm());
        profile.setWeightKg(request.getWeightKg());
        profile.setAge(request.getAge());
        profile.setGender(request.getGender());
        profile.setGoal(request.getGoal());
        profile.setActivityLevel(request.getActivityLevel());

        userProfileRepository.save(profile);
        return userMapper.toUserProfileResponse(profile);
    }


}