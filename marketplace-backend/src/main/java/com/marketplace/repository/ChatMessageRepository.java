package com.marketplace.repository;

import com.marketplace.model.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {

    // Lấy messages theo chat room (sắp xếp theo thời gian)
    List<ChatMessage> findByChatRoomIdOrderByCreatedAtAsc(String chatRoomId);

    // Lấy messages gần đây của chat room
    @Query("{'chatRoomId': ?0}")
    List<ChatMessage> findByChatRoomIdOrderByCreatedAtDesc(String chatRoomId);

    // Lấy messages chưa đọc của user trong room
    @Query("{'chatRoomId': ?0, 'senderId': {'$ne': ?1}, 'isRead': false}")
    List<ChatMessage> findUnreadMessagesByRoomAndUser(String chatRoomId, String userId);

    // Đếm messages chưa đọc
    @Query(value = "{'chatRoomId': ?0, 'senderId': {'$ne': ?1}, 'isRead': false}", count = true)
    long countUnreadMessagesByRoomAndUser(String chatRoomId, String userId);

    // Tìm messages theo sender
    List<ChatMessage> findBySenderIdOrderByCreatedAtDesc(String senderId);

    // Tìm messages theo type
    List<ChatMessage> findByMessageTypeAndChatRoomId(String messageType, String chatRoomId);

    // Tìm messages trong khoảng thời gian
    List<ChatMessage> findByChatRoomIdAndCreatedAtBetween(String chatRoomId, LocalDateTime start, LocalDateTime end);

    // Tìm message cuối cùng của room
    @Query(value = "{'chatRoomId': ?0}", sort = "{'createdAt': -1}")
    List<ChatMessage> findLastMessageByRoom(String chatRoomId);

    // Xóa tất cả messages của room
    void deleteByChatRoomId(String chatRoomId);
}