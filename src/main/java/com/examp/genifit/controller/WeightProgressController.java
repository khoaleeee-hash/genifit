package com.examp.genifit.controller;

import com.examp.genifit.common.response.ApiResponse;
import com.examp.genifit.dto.request.UpdateWeightProgressRequest;
import com.examp.genifit.dto.response.WeightProgressResponse;
import com.examp.genifit.service.WeightProgressService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/weight-progress")
@RequiredArgsConstructor
public class WeightProgressController {

    private final WeightProgressService weightProgressService;

    @PostMapping
    @Operation(summary = "Update current weight and calculate progress")
    public ResponseEntity<ApiResponse<WeightProgressResponse>> updateWeightProgress(
            @Valid @RequestBody UpdateWeightProgressRequest request
    ) {
        WeightProgressResponse response = weightProgressService.updateWeightProgress(request);

        return ResponseEntity.ok(ApiResponse.success("Update weight progress successfully", response));
    }
}