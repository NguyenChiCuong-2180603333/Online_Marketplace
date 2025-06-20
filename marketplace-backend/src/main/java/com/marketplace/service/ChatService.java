package com.marketplace.service;

import com.marketplace.model.ChatMessage;
import com.marketplace.model.Conversation;
import com.marketplace.repository.ChatMessageRepository;
import com.marketplace.repository.ConversationRepository;
import com.marketplace.exception.ResourceNotFoundException;
import com.marketplace.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class ChatService {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Tạo hoặc lấy conversation giữa 2 users
     */
    public Conversation getOrCreateConversation(String userId1, String userId2, String productId) {
        List<String> participants = Arrays.asList(userId1, userId2);
        participants.sort(String::compareTo); // Consistent ordering

        Optional<Conversation> existing = conversationRepository
                .findByParticipantIdsAndProductId(participants, productId);

        if (existing.isPresent()) {
            return existing.get();
        }

        // Create new conversation
        Conversation conversation = new Conversation(participants, productId);
        return conversationRepository.save(conversation);
    }

    /**
     * Gửi tin nhắn
     */
    public ChatMessage sendMessage(String conversationId, String senderId, String content, String messageType) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        // Verify sender is participant
        if (!conversation.getParticipantIds().contains(senderId)) {
            throw new BadRequestException("User not authorized for this conversation");
        }

        // Determine receiver
        String receiverId = conversation.getParticipantIds().stream()
                .filter(id -> !id.equals(senderId))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Receiver not found"));

        // Create message
        ChatMessage message = new ChatMessage(conversationId, senderId, receiverId, content);
        message.setMessageType(messageType);

        ChatMessage savedMessage = chatMessageRepository.save(message);

        // Update conversation
        conversation.setLastMessageId(savedMessage.getId());
        conversation.setLastMessageContent(content);
        conversation.setLastMessageTime(LocalDateTime.now());
        conversationRepository.save(conversation);

        // Send real-time notification
        messagingTemplate.convertAndSendToUser(
                receiverId,
                "/queue/messages",
                savedMessage
        );

        return savedMessage;
    }

    /**
     * Lấy messages trong conversation
     */
    public List<ChatMessage> getConversationMessages(String conversationId, String userId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        if (!conversation.getParticipantIds().contains(userId)) {
            throw new BadRequestException("User not authorized for this conversation");
        }

        return chatMessageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
    }

    /**
     * Lấy danh sách conversations của user
     */
    public List<Conversation> getUserConversations(String userId) {
        return conversationRepository.findByParticipantIdsContainingOrderByLastMessageTimeDesc(userId);
    }

    /**
     * Đánh dấu messages đã đọc
     */
    public void markMessagesAsRead(String conversationId, String userId) {
        List<ChatMessage> unreadMessages = chatMessageRepository
                .findByConversationIdAndReceiverIdAndIsReadFalse(conversationId, userId);

        unreadMessages.forEach(message -> message.setRead(true));
        chatMessageRepository.saveAll(unreadMessages);
    }

    /**
     * Đếm tin nhắn chưa đọc
     */
    public long getUnreadMessageCount(String userId) {
        return chatMessageRepository.countByReceiverIdAndIsReadFalse(userId);
    }

    /**
     * Xóa tin nhắn
     */
    public void deleteMessage(String messageId, String userId) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));

        if (!message.getSenderId().equals(userId)) {
            throw new BadRequestException("Only sender can delete message");
        }

        chatMessageRepository.delete(message);
    }
}
