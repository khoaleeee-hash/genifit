package com.examp.genifit.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ChatMessageDto {
    private Integer id;
    private String prompt;
    private String response;
    private LocalDateTime createdAt;
}
