package com.examp.genifit.controller;

import com.examp.genifit.dto.request.FoodEvaluationRequest;
import com.examp.genifit.dto.response.FoodEvaluationResponse;
import com.examp.genifit.dto.response.GeminiFoodScanResponse;
import com.examp.genifit.service.FoodEvaluationService;
import com.examp.genifit.service.GeminiFoodScanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/food-evaluation")
@RequiredArgsConstructor
public class FoodEvaluationController {
    private final GeminiFoodScanService geminiFoodScanService;
    private final FoodEvaluationService foodEvaluationService;

    @PostMapping(
            value = "/scan-image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public FoodEvaluationResponse scanImageAndEvalute(
            @RequestParam("image")MultipartFile image,
            @RequestParam(required = false)Integer userId,
            @RequestParam(required = false)Integer guestId
            ){
        GeminiFoodScanResponse scanResponse = geminiFoodScanService.scanFoodImage(image);

        FoodEvaluationRequest evaluationRequest = new FoodEvaluationRequest();
        evaluationRequest.setUserId(userId);
        evaluationRequest.setGuestId(guestId);
        evaluationRequest.setFoods(scanResponse.getFoods());
        evaluationRequest.setTotalCalories(scanResponse.getTotalCalories());
        evaluationRequest.setTotalCarbs(scanResponse.getTotalCarbs());
        evaluationRequest.setTotalFat(scanResponse.getTotalFat());
        evaluationRequest.setConfidence(scanResponse.getConfidence());
        evaluationRequest.setSource(scanResponse.getSource());
        evaluationRequest.setNote(scanResponse.getNote());

        return foodEvaluationService.evaluateScannedFood(evaluationRequest);
    }

    @PostMapping("/evalute")
    public FoodEvaluationResponse evaluatedScanFood(
            @RequestBody FoodEvaluationRequest request
    ){
        return foodEvaluationService.evaluateScannedFood(request);
    }

}
