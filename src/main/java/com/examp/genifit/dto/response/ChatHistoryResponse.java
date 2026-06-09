package com.examp.genifit.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ChatHistoryResponse {
    private List<ChatMessageDto> messages;
    private Integer nextCursor;   // null = đã hết tin
    private boolean hasMore;
}
