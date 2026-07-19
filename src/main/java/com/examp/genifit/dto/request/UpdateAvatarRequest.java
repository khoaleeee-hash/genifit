package com.examp.genifit.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateAvatarRequest {

    @NotBlank(message = "avatarUrl is required")
    String avatarUrl;

}