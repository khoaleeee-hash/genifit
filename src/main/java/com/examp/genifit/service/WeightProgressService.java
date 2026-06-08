package com.examp.genifit.service;

import com.examp.genifit.dto.request.UpdateWeightProgressRequest;
import com.examp.genifit.dto.response.WeightProgressResponse;

public interface WeightProgressService {

    WeightProgressResponse updateWeightProgress(
            UpdateWeightProgressRequest request
    );
}