package com.examp.genifit.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class ChatHistoryResponse {
    private List<ChatMessageDto> messages;
    private Integer nextCursor;   // null = đã hết tin
    private boolean hasMore;

    @Data
    @AllArgsConstructor
    public static class ChatMessageDto {
        private Integer id;
        private String prompt;
        private String response;
        private LocalDateTime createdAt;
    }

}
