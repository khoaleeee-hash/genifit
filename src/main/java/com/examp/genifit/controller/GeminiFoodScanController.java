package com.examp.genifit.controller;

import com.examp.genifit.dto.response.GeminiFoodScanResponse;
import com.examp.genifit.service.GeminiFoodScanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/gemini")
@RequiredArgsConstructor
public class GeminiFoodScanController {
    private final GeminiFoodScanService geminiFoodScanService;

    @PostMapping(
            value = "/scan-food-image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public GeminiFoodScanResponse scanFoodImage(@RequestParam("image")MultipartFile image){
        return geminiFoodScanService.scanFoodImage(image);
    }
}
