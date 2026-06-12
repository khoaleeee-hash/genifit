package com.examp.genifit.service;

import com.examp.genifit.dto.request.CreateUserRequest;
import com.examp.genifit.dto.response.UserResponse;

import java.util.List;

public interface UserService {
    UserResponse createUser(CreateUserRequest request);
    void deleteUserById(Integer id);
    UserResponse getUser(Integer id);
    void generateAndSendOtp(String email);
//    UserResponse updateUser(Integer userId, UserUpdateRequest request)

    List<UserResponse> getUsers();

}
