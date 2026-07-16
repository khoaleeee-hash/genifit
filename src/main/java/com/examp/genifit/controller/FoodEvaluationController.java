package com.examp.genifit.controller;

import com.examp.genifit.common.response.ApiResponse;
import com.examp.genifit.dto.request.FoodEvaluationRequest;
import com.examp.genifit.dto.response.FoodEvaluationResponse;
import com.examp.genifit.dto.response.GeminiFoodScanResponse;
import com.examp.genifit.entity.User;
import com.examp.genifit.repository.UserRepository;
import com.examp.genifit.service.FoodEvaluationService;
import com.examp.genifit.service.GeminiFoodScanService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/food-evaluation")
@RequiredArgsConstructor
@Tag(name = "Food Evaluation")
public class FoodEvaluationController {

    private final GeminiFoodScanService geminiFoodScanService;
    private final FoodEvaluationService foodEvaluationService;
    private final UserRepository userRepository;

//    @PostMapping(
//            value = "/scan-and-evaluate",
//            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
//    )
//    public FoodEvaluationResponse scanAndEvaluate(
//            @Parameter(
//                    description = "Food image file",
//                    required = true,
//                    schema = @Schema(type = "string", format = "binary")
//            )
//            @RequestParam("image") MultipartFile image,
//            @RequestParam(required = false) Integer guestId
//    ) {
//        Integer userId = getCurrentUserIdOrNull();
//
//        if (userId == null && guestId == null) {
//            throw new RuntimeException("Please login or provide guestId");
//        }
//
//        if (userId != null) {
//            guestId = null;
//        }
//
//        GeminiFoodScanResponse scanResponse = geminiFoodScanService.scanFoodImage(
//                image,
//                userId,
//                guestId
//        );
//
//        FoodEvaluationRequest evaluationRequest = new FoodEvaluationRequest();
//        evaluationRequest.setUserId(userId);
//        evaluationRequest.setGuestId(guestId);
//        evaluationRequest.setFoods(scanResponse.getFoods());
//        evaluationRequest.setTotalCalories(scanResponse.getTotalCalories());
//        evaluationRequest.setTotalProtein(scanResponse.getTotalProtein());
//        evaluationRequest.setTotalCarbs(scanResponse.getTotalCarbs());
//        evaluationRequest.setTotalFat(scanResponse.getTotalFat());
//        evaluationRequest.setConfidence(scanResponse.getConfidence());
//        evaluationRequest.setSource(scanResponse.getSource());
//        evaluationRequest.setNote(scanResponse.getNote());
//
//        return foodEvaluationService.evaluateScannedFood(evaluationRequest);
//    }
@PostMapping(
        value = "/scan-and-evaluate",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
)
public ResponseEntity<ApiResponse<FoodEvaluationResponse>> scanAndEvaluate(
        @RequestPart("image") MultipartFile image,
        @RequestParam(name = "guestId", required = false) Integer guestId
) {
    try {
        if (image == null || image.isEmpty()) {
            throw new RuntimeException("Image file is required");
        }

        Integer userId = getCurrentUserIdOrNull();

        if (userId == null && guestId == null) {
            throw new RuntimeException("Please login or provide guestId");
        }

        if (userId != null) {
            guestId = null;
        }

        GeminiFoodScanResponse scanResponse =
                geminiFoodScanService.scanFoodImage(image, userId, guestId);

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

        FoodEvaluationResponse response = foodEvaluationService.evaluateScannedFood(evaluationRequest);

        return ResponseEntity.ok(ApiResponse.success("Scan và đánh giá món ăn thành công", response));

    } catch (RuntimeException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.fail(e.getMessage(), null));
    }
}

    @PostMapping("/evaluate")
    public FoodEvaluationResponse evaluateScannedFood(
            @RequestBody FoodEvaluationRequest request
    ) {
        Integer userId = getCurrentUserIdOrNull();

        if (userId != null) {
            request.setUserId(userId);
            request.setGuestId(null);
        }

        if (request.getUserId() == null && request.getGuestId() == null) {
            throw new RuntimeException("Please login or provide guestId");
        }

        return foodEvaluationService.evaluateScannedFood(request);
    }

    private Integer getCurrentUserIdOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        if (principal == null || principal.equals("anonymousUser")) {
            return null;
        }

        String username = authentication.getName();

        return userRepository.findByUsernameAndIsActiveTrue(username)
                .map(User::getUserId)
                .orElse(null);
    }
}