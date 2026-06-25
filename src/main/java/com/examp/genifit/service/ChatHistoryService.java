package com.examp.genifit.service;

import com.examp.genifit.dto.response.ChatHistoryResponse;
import com.examp.genifit.entity.User;

public interface ChatHistoryService {
    ChatHistoryResponse getHistory(User user, Integer cursorId, int pageSize);
}
