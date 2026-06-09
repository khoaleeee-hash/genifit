package com.examp.genifit.repository;

import com.examp.genifit.entity.AIChatHistory;
import com.examp.genifit.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;


public interface AIChatHistoryRepository extends JpaRepository<AIChatHistory, Integer> {
    //      Lần đầu load — lấy N tin mới nhất
    List<AIChatHistory> findByUserOrderByChatIdDesc(User user, Pageable pageable);
    // Scroll lên load thêm — lấy N tin cũ hơn cursor
    List<AIChatHistory> findByUserAndChatIdLessThanOrderByChatIdDesc(User user, Integer cursorId, Pageable pageable);

    //
    List<AIChatHistory> findTopByUserOrderByCreatedAtDesc(User user, int maxHistory);
    // Xóa toàn bộ chat của 1 user
    void deleteByUser(User user);

    // Đếm số tin nhắn
    long countByUser(User user);

//    @Query("""
//    SELECT c
//    FROM AIChatHistory c
//    WHERE c.user.id = :userId
//      AND c.chatId < :cursorId
//    ORDER BY c.chatId DESC
//""")
//    List<AIChatHistory> findHistory(
//            @Param("userId") Integer userId,
//            @Param("cursorId") Integer cursorId,
//            Pageable pageable
//    );

}
