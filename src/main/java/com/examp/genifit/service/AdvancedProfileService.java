package com.examp.genifit.service;

import com.examp.genifit.dto.request.CreateAdvancedProfileRequest;
import com.examp.genifit.dto.request.UpdateAdvancedProfileRequest;
import com.examp.genifit.dto.response.AdvancedProfileResponse;

public interface AdvancedProfileService {

    AdvancedProfileResponse createAdvancedProfile(
            Integer userId,
            CreateAdvancedProfileRequest request
    );

    AdvancedProfileResponse updateAdvancedProfile(
            Integer userId,
            UpdateAdvancedProfileRequest request
    );

    AdvancedProfileResponse getMyAdvancedProfile(
            Integer userId
    );

}