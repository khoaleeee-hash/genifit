package com.examp.genifit.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GuestLoginRequest {
    @NotBlank(message = "Device ID is not null")
    private String deviceId;
    private String username;
}