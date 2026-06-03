package com.examp.genifit.controller;

import com.examp.genifit.dto.request.AddManualFoodRequest;
import com.examp.genifit.dto.response.AddManualFoodResponse;
import com.examp.genifit.service.DailyLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/daily-logs")
@RequiredArgsConstructor

public class DailyLogController {
    private final DailyLogService dailyLogService;

    @PostMapping("/meals/manual-foods")
    public AddManualFoodResponse addManualFood(
            @RequestBody AddManualFoodRequest request
            ){
        return dailyLogService.addManualFood(request);
    }
}
