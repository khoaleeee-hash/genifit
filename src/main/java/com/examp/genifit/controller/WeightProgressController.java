package com.examp.genifit.controller;

import com.examp.genifit.common.response.ApiResponse;
import com.examp.genifit.common.security.JwtUtils;
import com.examp.genifit.dto.request.UpdateWeightProgressRequest;
import com.examp.genifit.dto.response.WeightProgressHistoryResponse;
import com.examp.genifit.dto.response.WeightProgressResponse;
import com.examp.genifit.service.WeightProgressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/weight-progress")
@RequiredArgsConstructor
@Tag(name = "Weight Progress")
public class WeightProgressController {

    private final WeightProgressService weightProgressService;
    private final JwtUtils jwtUtils;

    @PostMapping
    @Operation(summary = "Update current weight and calculate progress")
    public ResponseEntity<ApiResponse<WeightProgressResponse>> updateWeightProgress(
            @Valid @RequestBody UpdateWeightProgressRequest request
    ) {
        WeightProgressResponse response = weightProgressService.updateWeightProgress(request);

        return ResponseEntity.ok(ApiResponse.success("Update weight progress successfully", response));
    }

    @GetMapping("/history")
    @Operation(summary = "Get weight progress history")
    public ResponseEntity<ApiResponse<List<WeightProgressHistoryResponse>>> getWeightProgressHistory(
            @AuthenticationPrincipal Jwt jwt,

            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "Page number must be greater than or equal to 0")
            Integer pageNum,

            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "Page size must be greater than or equal to 1")
            @Max(value = 100, message = "Page size must be less than or equal to 100")
            Integer pageSize
    ) {
        Integer userId = jwtUtils.getUserId(jwt);

        Page<WeightProgressHistoryResponse> page = weightProgressService.getWeightProgressHistory(userId, pageNum, pageSize);

        return ResponseEntity.ok(ApiResponse.successPage("Get weight progress history successfully", page));
    }
}