package com.examp.genifit.service;

import com.examp.genifit.dto.request.AddManualFoodRequest;
import com.examp.genifit.dto.response.AddManualFoodResponse;

public interface DailyLogService {
    AddManualFoodResponse addManualFood(AddManualFoodRequest request);
}
