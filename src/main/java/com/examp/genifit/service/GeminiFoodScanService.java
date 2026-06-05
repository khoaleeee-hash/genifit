package com.examp.genifit.service;

import com.examp.genifit.dto.response.GeminiFoodScanResponse;
import org.springframework.web.multipart.MultipartFile;

public interface GeminiFoodScanService {
    GeminiFoodScanResponse scanFoodImage(MultipartFile image);
}
