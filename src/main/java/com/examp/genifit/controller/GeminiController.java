package com.examp.genifit.controller;

import com.examp.genifit.dto.request.GeminiMealSuggestionRequest;
import com.examp.genifit.dto.response.GeminiFoodScanResponse;
import com.examp.genifit.dto.response.GeminiMealSuggestionResponse;
import com.examp.genifit.service.GeminiFoodScanService;
import com.examp.genifit.service.GeminiMealSuggestionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/gemini")
@RequiredArgsConstructor
@Tag(name = "Gemini")
public class GeminiController {

    private final GeminiFoodScanService geminiFoodScanService;
    private final GeminiMealSuggestionService geminiMealSuggestionService;

    @PostMapping(
            value = "/scan-food-image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public GeminiFoodScanResponse scanFoodImage(
            @RequestParam("image") MultipartFile image,
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) Integer guestId
    ) {
        return geminiFoodScanService.scanFoodImage(image, userId, guestId);
    }

}