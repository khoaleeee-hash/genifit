package com.examp.genifit.mapper;

import com.examp.genifit.dto.request.CreateUserRequest;
import com.examp.genifit.dto.response.UserResponse;
import com.examp.genifit.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(CreateUserRequest request);

    UserResponse toUserResponse(User user);

}
