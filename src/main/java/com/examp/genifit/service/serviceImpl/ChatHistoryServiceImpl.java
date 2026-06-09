package com.examp.genifit.service.serviceImpl;

import com.examp.genifit.dto.response.ChatHistoryResponse;
import com.examp.genifit.dto.response.ChatMessageDto;
import com.examp.genifit.entity.AIChatHistory;
import com.examp.genifit.entity.User;
import com.examp.genifit.repository.AIChatHistoryRepository;
import com.examp.genifit.repository.UserRepository;
import com.examp.genifit.service.ChatHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatHistoryServiceImpl implements ChatHistoryService {
    private final AIChatHistoryRepository chatHistoryRepository;
    private final UserRepository userRepository;
    private static final int DEFAULT_PAGE_SIZE = 20;

    @Override
    public ChatHistoryResponse getHistory(User user, Integer cursorId, int pageSize) {
        int size = pageSize > 0 ? Math.min(pageSize, 50) : DEFAULT_PAGE_SIZE; // chặn client request quá lớn
        Pageable pageable = PageRequest.of(0, size + 1); // lấy thừa 1 để biết còn data không
        List<AIChatHistory> raw = (cursorId == null)
                ? chatHistoryRepository.findByUserOrderByChatIdDesc(user, pageable)
                : chatHistoryRepository.findByUserAndChatIdLessThanOrderByChatIdDesc(user, cursorId, pageable);

        // Nếu lấy được size+1 tức là còn data phía sau
        boolean hasMore = raw.size() > size;
        if (hasMore) raw = raw.subList(0, size); // bỏ phần tử thừa

        // Reverse lại: DB trả DESC (mới→cũ), client cần ASC (cũ→mới) để render đúng
        Collections.reverse(raw);

        List<ChatMessageDto> dtos = raw.stream()
                .map(m -> new ChatMessageDto(m.getChatId(), m.getPrompt(), m.getResponse(), m.getCreatedAt()))
                .toList();

        Integer nextCursor = hasMore ? raw.get(0).getChatId() : null; // id nhỏ nhất = tin cũ nhất trong batch

        return new ChatHistoryResponse(dtos, nextCursor, hasMore);

    }
}
