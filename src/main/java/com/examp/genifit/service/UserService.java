package com.examp.genifit.service;

import com.examp.genifit.dto.request.ChangePasswordRequest;
import com.examp.genifit.dto.request.AssignSubscriptionRequest;
import com.examp.genifit.dto.request.CreateUserRequest;
import com.examp.genifit.dto.request.ResetPasswordRequest;
import com.examp.genifit.dto.request.UpdateUserProfileRequest;
import com.examp.genifit.dto.response.CancelSubscriptionResponse;
import com.examp.genifit.dto.response.UserProfileResponse;
import com.examp.genifit.dto.response.UserResponse;
import com.examp.genifit.dto.response.UserSubscriptionResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {
    UserResponse createUser(CreateUserRequest request);

    UserResponse getUser(Integer id);

    void generateAndSendOtp(String email);

    // UserResponse updateUser(Integer userId, UserUpdateRequest request)
    UserResponse getMyInfo();

    List<UserResponse> searchUsers(String keyword);

    List<UserResponse> getUsers();

    void changePassword(ChangePasswordRequest request);

    void resetPassword(ResetPasswordRequest request);

    void deleteMe();

    void deleteUserById(Integer id);

    void restoreUserById(Integer id);

    void generateAndSendOtpForForgotPassword(String email);

    UserProfileResponse updateMyProfile(UpdateUserProfileRequest request);

    UserProfileResponse updateUserProfileByAdmin(Integer userId, UpdateUserProfileRequest request);

    void updateAvatarUrl(String avatarUrl);

    void updateAvatarUrlByAdmin(Integer userId, String avatarUrl);

    UserSubscriptionResponse assignSubscription(AssignSubscriptionRequest request);

}
