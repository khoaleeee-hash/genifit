package com.examp.genifit.service.serviceImpl;

import com.examp.genifit.common.exception.ApiException;
import com.examp.genifit.common.exception.ErrorCode;
import com.examp.genifit.dto.request.ChangePasswordRequest;
import com.examp.genifit.dto.request.AssignSubscriptionRequest;
import com.examp.genifit.dto.request.CreateUserRequest;
import com.examp.genifit.dto.request.ResetPasswordRequest;
import com.examp.genifit.dto.request.UpdateUserProfileRequest;
import com.examp.genifit.dto.response.*;
import com.examp.genifit.entity.OtpToken;
import com.examp.genifit.entity.User;
import com.examp.genifit.entity.UserProfile;
import com.examp.genifit.entity.UserRole;
import com.examp.genifit.mapper.UserMapper;
import com.examp.genifit.repository.OtpTokenRepository;
import com.examp.genifit.repository.UserProfileRepository;
import com.examp.genifit.entity.*;
import com.examp.genifit.repository.SubscriptionPlanRepository;
import com.examp.genifit.repository.UserRepository;
import com.examp.genifit.repository.UserSubscriptionRepository;
import com.examp.genifit.service.EmailService;
import com.examp.genifit.service.MoMoService;
import com.examp.genifit.service.UserService;
import com.examp.genifit.util.CalorieCalculatorUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.math.RoundingMode;

import java.time.LocalDate;
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
    MoMoService  moMoService;

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

        SubscriptionPlan freePlan = subscriptionPlanRepository.findFirstByPlanType(PlanType.FREE)
                .orElseThrow(() -> new RuntimeException("Hệ thống chưa cấu hình gói FREE mặc định!"));

        UserSubscription defaultSubscription = UserSubscription.builder()
                .user(user)
                .subscriptionPlan(freePlan)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusYears(100))
                .status(SubscriptionStatus.ACTIVE)
                .autoRenew(true)
                .build();
        userSubscriptionRepository.save(defaultSubscription);

        otpTokenRepository.delete(validOtp);

        return userMapper.toUserResponse(user);
    }

    @Override
    public UserResponse getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String currentUsername = context.getAuthentication().getName();

        User user = userRepository.findByUsernameAndIsActiveTrue(currentUsername)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        UserResponse response = userMapper.toUserResponse(user);

        userProfileRepository.findByUser(user).ifPresent(profile -> {
            response.setUserProfile(userMapper.toUserProfileResponse(profile));
        });

        return response;
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

    @Transactional
    public UserSubscriptionResponse assignSubscription(AssignSubscriptionRequest request) {
        if (request == null || request.getUserId() == null || request.getPlanId() == null) {
            throw new ApiException(
                    ErrorCode.INVALID_SUBSCRIPTION_REQUEST,
                    "Gói đăng kí này không hợp lệ hoặc không tồn tại"
            );
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        SubscriptionPlan plan = subscriptionPlanRepository.findById(request.getPlanId())
                .orElseThrow(() -> new ApiException(ErrorCode.SUBSCRIPTION_PLAN_NOT_FOUND));

        if (plan.getActive() == null || !plan.getActive()) {
            throw new ApiException(
                    ErrorCode.SUBSCRIPTION_PLAN_INACTIVE,
                    "Gói đăng kí này hiện không còn hoạt động"
            );
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

        return UserSubscriptionResponse.builder()
                .subscriptionId(savedSubscription.getSubscriptionId())
                .userId(savedSubscription.getUser().getUserId())
                .username(savedSubscription.getUser().getUsername())
                .planId(savedSubscription.getSubscriptionPlan().getPlanId())
                .planType(savedSubscription.getSubscriptionPlan().getPlanType())
                .planName(savedSubscription.getSubscriptionPlan().getPlanName())
                .startDate(savedSubscription.getStartDate())
                .endDate(savedSubscription.getEndDate())
                .status(savedSubscription.getStatus())
                .autoRenew(savedSubscription.getAutoRenew())
                .cancelledAt(savedSubscription.getCancelledAt())
                .createdAt(savedSubscription.getCreatedAt())
                .updatedAt(savedSubscription.getUpdatedAt())
                .build();    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ApiException(
                    ErrorCode.UNAUTHENTICATED,
                    "Người dùng chưa đăng nhập"
            );
        }

        Object principal = authentication.getPrincipal();

        if (principal == null || principal.equals("anonymousUser")) {
            throw new ApiException(
                    ErrorCode.UNAUTHENTICATED,
                    "Người dùng chưa đăng nhập"
            );
        }

        String username = authentication.getName();

        return userRepository.findByUsernameAndIsActiveTrue(username)
                .orElseThrow(() -> new ApiException(
                        ErrorCode.USER_NOT_FOUND,
                        "Không tìm thấy người dùng"
                ));
    }

    @Override
    @Transactional
    public void deleteMe() {
        var context = SecurityContextHolder.getContext();
        String currentUsername = context.getAuthentication().getName();

        User user = userRepository.findByUsernameAndIsActiveTrue(currentUsername)
                .orElseThrow(() -> new ApiException(
                        ErrorCode.USER_NOT_FOUND,
                        "Không tìm thấy người dùng"
                ));

        user.setIsActive(false);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void generateAndSendOtpForForgotPassword(String email) {
        if (!userRepository.existsByEmailAndIsActiveTrue(email)) {
            throw new ApiException(
                    ErrorCode.USER_NOT_FOUND,
                    "Không tìm thấy người dùng"
            );
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
                .orElseThrow(() -> new ApiException(
                        ErrorCode.USER_NOT_FOUND,
                        "Không tìm thấy người dùng"
                ));

        OtpToken validOtp = otpTokenRepository.findByEmailAndOtpCode(request.getEmail(), request.getOtpCode())
                .orElseThrow(() -> new ApiException(
                        ErrorCode.INVALID_OTP,
                        "Mã OTP không hợp lệ"
                ));

        if (validOtp.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new ApiException(
                    ErrorCode.OTP_EXPIRED,
                    "Mã OTP đã hết hạn"
            );
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
                .orElseThrow(() -> new ApiException(
                        ErrorCode.USER_NOT_FOUND,
                        "Không tìm thấy người dùng"
                ));

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
                .orElseThrow(() -> new ApiException(
                        ErrorCode.USER_NOT_FOUND,
                        "Không tìm thấy người dùng"
                ));

        user.setIsActive(false);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public UserProfileResponse updateMyProfile(UpdateUserProfileRequest request) {
        var context = SecurityContextHolder.getContext();
        String currentUsername = context.getAuthentication().getName();

        User user = userRepository.findByUsernameAndIsActiveTrue(currentUsername)
                .orElseThrow(() -> new ApiException(
                        ErrorCode.USER_NOT_FOUND,
                        "Không tìm thấy người dùng"
                ));

        UserProfile profile = userProfileRepository.findByUser(user)
                .orElse(new UserProfile());

        profile.setUser(user);


        boolean isFirstTimeSettingGoal = profile.getTargetDate() == null;

        boolean goalChanged = isFirstTimeSettingGoal ||
                (request.getTargetWeightKg() != null && !request.getTargetWeightKg().equals(profile.getTargetWeightKg())) ||
                (request.getTargetDate() != null && !request.getTargetDate().equals(profile.getTargetDate()));

        if (goalChanged && request.getTargetWeightKg() != null && request.getTargetDate() != null) {
            profile.setInitialWeight(request.getWeightKg());
            profile.setGoalStartDate(LocalDate.now());
        }

        profile.setHeightCm(request.getHeightCm());
        profile.setWeightKg(request.getWeightKg());
        profile.setAge(request.getAge());
        profile.setGender(request.getGender());
        profile.setGoal(request.getGoal());
        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());
        profile.setDateOfBirth(request.getDateOfBirth());
        profile.setOccupation(request.getOccupation());
        profile.setActivityLevel(request.getActivityLevel());

        profile.setTargetWeightKg(request.getTargetWeightKg());
        profile.setTargetDate(request.getTargetDate());
        profile.setMedicalConditions(request.getMedicalConditions());
        profile.setAllergies(request.getAllergies());

        Double calculatedCalorie = CalorieCalculatorUtil.calculateTargetCalorie(
                request.getWeightKg(),
                request.getHeightCm(),
                request.getAge(),
                request.getGender(),
                request.getActivityLevel(),
                request.getGoal()
        );
        profile.setBaseTargetCalorie(calculatedCalorie);

        userProfileRepository.save(profile);

        return userMapper.toUserProfileResponse(profile);
    }

    private record RefundResult(
            Long usedDays,
            RefundStatus refundStatus,
            Integer refundPercent,
            BigDecimal refundAmount,
            String message
    ) {
    }
}