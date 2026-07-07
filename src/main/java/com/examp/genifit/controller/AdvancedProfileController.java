package com.examp.genifit.controller;

import com.examp.genifit.common.response.ApiResponse;
import com.examp.genifit.common.security.JwtUtils;
import com.examp.genifit.dto.request.CreateAdvancedProfileRequest;
import com.examp.genifit.dto.request.UpdateAdvancedProfileRequest;
import com.examp.genifit.dto.response.AdvancedProfileResponse;
import com.examp.genifit.service.AdvancedProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/advanced-profile")
@RequiredArgsConstructor
@Tag(name = "Advanced Profile")
public class AdvancedProfileController {

    private final AdvancedProfileService advancedProfileService;
    private final JwtUtils jwtUtils;

    @PostMapping
    @Operation(summary = "Create advanced profile")
    public ResponseEntity<ApiResponse<AdvancedProfileResponse>> createAdvancedProfile(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateAdvancedProfileRequest request
    ) {

        Integer userId = jwtUtils.getUserId(jwt);

        AdvancedProfileResponse response =
                advancedProfileService.createAdvancedProfile(
                        userId,
                        request
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Create advanced profile successfully",
                        response
                )
        );

    }

    @PutMapping
    @Operation(summary = "Update advanced profile")
    public ResponseEntity<ApiResponse<AdvancedProfileResponse>> updateAdvancedProfile(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdateAdvancedProfileRequest request
    ) {

        Integer userId = jwtUtils.getUserId(jwt);

        AdvancedProfileResponse response =
                advancedProfileService.updateAdvancedProfile(
                        userId,
                        request
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Update advanced profile successfully",
                        response
                )
        );

    }

    @GetMapping("/me")
    @Operation(summary = "Get my advanced profile")
    public ResponseEntity<ApiResponse<AdvancedProfileResponse>> getMyAdvancedProfile(
            @AuthenticationPrincipal Jwt jwt
    ) {

        Integer userId = jwtUtils.getUserId(jwt);

        AdvancedProfileResponse response =
                advancedProfileService.getMyAdvancedProfile(userId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Get advanced profile successfully",
                        response
                )
        );

    }

    @DeleteMapping
    @Operation(summary = "Delete advanced profile")
    public ResponseEntity<ApiResponse<Void>> deleteAdvancedProfile(
            @AuthenticationPrincipal Jwt jwt
    ) {

        Integer userId = jwtUtils.getUserId(jwt);

        advancedProfileService.deleteAdvancedProfile(userId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Delete advanced profile successfully",
                        null
                )
        );

    }

}