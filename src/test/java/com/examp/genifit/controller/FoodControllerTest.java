package com.examp.genifit.controller;

import com.examp.genifit.dto.request.FoodFilterRequest;
import com.examp.genifit.dto.response.FoodResponse;
import com.examp.genifit.dto.response.PageResponse;
import com.examp.genifit.service.FoodService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FoodControllerTest {

    @Mock
    private FoodService foodService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new FoodController(foodService)).build();
    }

    @Test
    void filterFoods_bindsOptionalFilterAndReturnsPage() throws Exception {
        when(foodService.filterFoods(any())).thenReturn(emptyPage());

        mockMvc.perform(post("/api/foods/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"searchCondition\":{\"keyword\":\"rice\"},\"pageInfo\":{\"pageNum\":1,\"pageSize\":10}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        ArgumentCaptor<FoodFilterRequest> request = ArgumentCaptor.forClass(FoodFilterRequest.class);
        verify(foodService).filterFoods(request.capture());
        assertEquals("rice", request.getValue().getSearchCondition().getKeyword());
    }

    @Test
    void filterFoods_acceptsMissingBodyBecauseRequestIsOptional() throws Exception {
        when(foodService.filterFoods(null)).thenReturn(emptyPage());

        mockMvc.perform(post("/api/foods/search"))
                .andExpect(status().isOk());

        verify(foodService).filterFoods(null);
    }

    private PageResponse<FoodResponse> emptyPage() {
        return PageResponse.<FoodResponse>builder().content(List.of()).build();
    }
}
