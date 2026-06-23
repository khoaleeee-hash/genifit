package com.examp.genifit.dto.response;

import com.examp.genifit.entity.UserRole;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    Integer userId;
    String username;
    String email;
    UserRole role;

    UserProfileResponse userProfile;
}
