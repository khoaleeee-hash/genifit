package com.examp.genifit.service;

import com.examp.genifit.dto.request.UpdateWeightProgressRequest;
import com.examp.genifit.dto.response.WeightProgressHistoryResponse;
import com.examp.genifit.dto.response.WeightProgressResponse;
import org.springframework.data.domain.Page;

public interface WeightProgressService {

    WeightProgressResponse updateWeightProgress(UpdateWeightProgressRequest request);

    Page<WeightProgressHistoryResponse> getWeightProgressHistory(Integer userId, Integer pageNum, Integer pageSize);
}