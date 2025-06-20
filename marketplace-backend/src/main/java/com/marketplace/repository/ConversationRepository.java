package com.marketplace.repository;

import com.marketplace.model.Conversation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends MongoRepository<Conversation, String> {

    List<Conversation> findByParticipantIdsContainingOrderByLastMessageTimeDesc(String userId);

    @Query("{'participantIds': {'$all': ?0}, 'productId': ?1}")
    Optional<Conversation> findByParticipantIdsAndProductId(List<String> participantIds, String productId);

    @Query("{'participantIds': {'$all': ?0}}")
    List<Conversation> findByParticipantIds(List<String> participantIds);

    // Find conversations for a specific product
    List<Conversation> findByProductId(String productId);

    // Find active conversations
    List<Conversation> findByIsActiveTrueOrderByLastMessageTimeDesc();

    // Find conversations that haven't had messages recently
    @Query("{'lastMessageTime': {'$lt': ?0}, 'isActive': true}")
    List<Conversation> findInactiveConversations(java.time.LocalDateTime cutoffDate);

    // Count conversations for user
    @Query(value = "{'participantIds': ?0}", count = true)
    long countByParticipantIdsContaining(String userId);
}