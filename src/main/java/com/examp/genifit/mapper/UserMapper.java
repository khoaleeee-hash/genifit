package com.examp.genifit.mapper;

import com.examp.genifit.dto.request.CreateUserRequest;
import com.examp.genifit.dto.response.UserProfileResponse;
import com.examp.genifit.dto.response.UserResponse;
import com.examp.genifit.entity.User;
import com.examp.genifit.entity.UserProfile;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(CreateUserRequest request);
    UserResponse toUserResponse(User user);
    UserProfileResponse toUserProfileResponse(UserProfile userProfile);
}