package com.examp.genifit.controller;

import com.examp.genifit.dto.request.FoodEvaluationRequest;
import com.examp.genifit.dto.response.FoodEvaluationResponse;
import com.examp.genifit.dto.response.GeminiFoodScanResponse;
import com.examp.genifit.service.FoodEvaluationService;
import com.examp.genifit.service.GeminiFoodScanService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/food-evaluation")
@RequiredArgsConstructor
@Tag(name = "Food Evaluation")
public class FoodEvaluationController {

    private final GeminiFoodScanService geminiFoodScanService;
    private final FoodEvaluationService foodEvaluationService;

    @PostMapping(
            value = "/scan-and-evaluate",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public FoodEvaluationResponse scanAndEvaluate(
            @RequestParam("image") MultipartFile image,
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) Integer guestId
    ) {
        GeminiFoodScanResponse scanResponse = geminiFoodScanService.scanFoodImage(
                image,
                userId,
                guestId
        );

        FoodEvaluationRequest evaluationRequest = new FoodEvaluationRequest();
        evaluationRequest.setUserId(userId);
        evaluationRequest.setGuestId(guestId);
        evaluationRequest.setFoods(scanResponse.getFoods());
        evaluationRequest.setTotalCalories(scanResponse.getTotalCalories());
        evaluationRequest.setTotalProtein(scanResponse.getTotalProtein());
        evaluationRequest.setTotalCarbs(scanResponse.getTotalCarbs());
        evaluationRequest.setTotalFat(scanResponse.getTotalFat());
        evaluationRequest.setConfidence(scanResponse.getConfidence());
        evaluationRequest.setSource(scanResponse.getSource());
        evaluationRequest.setNote(scanResponse.getNote());

        return foodEvaluationService.evaluateScannedFood(evaluationRequest);
    }

    @PostMapping("/evaluate")
    public FoodEvaluationResponse evaluateScannedFood(
            @RequestBody FoodEvaluationRequest request
    ) {
        return foodEvaluationService.evaluateScannedFood(request);
    }
}