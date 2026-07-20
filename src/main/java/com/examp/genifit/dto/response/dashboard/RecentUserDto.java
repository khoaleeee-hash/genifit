package com.examp.genifit.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentUserDto {
    private Integer userId;
    private String username;
    private String email;
    private String role;
    private String joinDate;
    private String avatar;
}
