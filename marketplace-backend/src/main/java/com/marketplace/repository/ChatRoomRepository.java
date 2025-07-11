package com.marketplace.repository;

import com.marketplace.model.ChatRoom;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends MongoRepository<ChatRoom, String> {

    Optional<ChatRoom> findBySellerIdAndBuyerIdAndProductId(String sellerId, String buyerId, String productId);

    @Query("{'participants': ?0, 'active': true}")
    List<ChatRoom> findByParticipantsContainingAndActiveTrue(String userId);

    List<ChatRoom> findBySellerIdAndActiveTrue(String sellerId);

    List<ChatRoom> findByBuyerIdAndActiveTrue(String buyerId);

    List<ChatRoom> findByProductIdAndActiveTrue(String productId);

    List<ChatRoom> findByRoomTypeAndActiveTrue(String roomType);

    @Query(value = "{'participants': ?0, 'active': true}", count = true)
    long countActiveRoomsByUser(String userId);
}