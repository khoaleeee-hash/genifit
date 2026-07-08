package com.examp.genifit.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateUserFromGuestRequest {
    @NotBlank(message = "Tên đăng nhập không được để trống")
    String username;

    @Size(min = 8, message = "INVALID_PASSWORD")
    String passwordHash;

    @NotNull
    String email;

    String otpCode;
}
