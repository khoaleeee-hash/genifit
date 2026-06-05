package com.examp.genifit.service;

import com.examp.genifit.dto.request.FoodEvaluationRequest;
import com.examp.genifit.dto.response.FoodEvaluationResponse;

public interface FoodEvaluationService {
    FoodEvaluationResponse evaluateScannedFood(FoodEvaluationRequest request);
}
