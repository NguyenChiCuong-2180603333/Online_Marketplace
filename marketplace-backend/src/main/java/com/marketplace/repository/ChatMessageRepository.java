package com.marketplace.repository;

import com.marketplace.model.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {

    List<ChatMessage> findByConversationIdOrderByCreatedAtAsc(String conversationId);

    List<ChatMessage> findByConversationIdAndReceiverIdAndIsReadFalse(String conversationId, String receiverId);

    long countByReceiverIdAndIsReadFalse(String receiverId);

    List<ChatMessage> findBySenderIdOrReceiverIdOrderByCreatedAtDesc(String senderId, String receiverId);

    @Query("{'conversationId': ?0, 'createdAt': {'$gte': ?1, '$lte': ?2}}")
    List<ChatMessage> findByConversationIdAndDateRange(String conversationId, LocalDateTime start, LocalDateTime end);

    // Find latest message in conversation
    @Query(value = "{'conversationId': ?0}", sort = "{'createdAt': -1}")
    List<ChatMessage> findLatestByConversationId(String conversationId);

    // Delete messages older than specified date
    void deleteByCreatedAtBefore(LocalDateTime date);

    // Statistics
    long countByConversationId(String conversationId);

    @Query(value = "{'senderId': ?0, 'createdAt': {'$gte': ?1}}", count = true)
    long countMessagesSentByUserSince(String userId, LocalDateTime since);

    void deleteByConversationId(String conversationId);
}