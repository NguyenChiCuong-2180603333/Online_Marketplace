package com.marketplace.controller;

import com.marketplace.dto.ChatMessageRequest;
import com.marketplace.model.ChatMessage;
import com.marketplace.model.Conversation;
import com.marketplace.repository.ConversationRepository;
import com.marketplace.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@Controller
public class ChatWebSocketController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessageRequest request, Principal principal, Message<?> message) {
        if (principal == null) {
            SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.wrap(message);
            principal = (Principal) accessor.getHeader("user");
            if (principal == null) {
                principal = (Principal) accessor.getHeader("simpUser");
            }
            if (principal == null) {
                System.err.println(" Principal is still null in sendMessage");
                return;
            }
        }
        
        String userId = principal.getName();
    
        String conversationId = request.getConversationId();
        String content = request.getContent();
        String messageType = request.getMessageType() != null ? request.getMessageType() : "TEXT";
    
        ChatMessage savedMessage = chatService.sendMessage(conversationId, userId, content, messageType);
        
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElse(null);
        
        if (conversation != null) {
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("id", savedMessage.getId());
            messageData.put("conversationId", savedMessage.getConversationId());
            messageData.put("senderId", savedMessage.getSenderId());
            messageData.put("receiverId", savedMessage.getReceiverId());
            messageData.put("content", savedMessage.getContent());
            messageData.put("messageType", savedMessage.getMessageType());
            messageData.put("attachmentUrl", savedMessage.getAttachmentUrl());
            messageData.put("isRead", savedMessage.isRead());
            messageData.put("isEdited", savedMessage.isEdited());
            messageData.put("createdAt", savedMessage.getCreatedAt());
            messageData.put("editedAt", savedMessage.getEditedAt());
            
            for (String participantId : conversation.getParticipantIds()) {
                System.out.println("[WebSocket] Broadcast to user: " + participantId + " data: " + messageData);
                messagingTemplate.convertAndSendToUser(
                    participantId,
                    "/queue/messages",
                    messageData
                );
            }
            messagingTemplate.convertAndSend("/topic/chat", messageData);
            
            System.out.println("💬 Message sent to participants: " + conversation.getParticipantIds());
        }
    }

    @MessageMapping("/chat.join")
    @SendTo("/topic/chat")
    public Map<String, Object> joinRoom(@Payload Map<String, String> payload, Principal principal) {
        Map<String, Object> response = new HashMap<>();
        if (principal == null) {
            System.err.println(" Principal is null in joinRoom");
            response.put("error", "Authentication required");
            return response;
        }
        String conversationId = payload.get("conversationId");
        String userId = principal.getName();
        response.put("type", "USER_JOINED");
        response.put("conversationId", conversationId);
        response.put("userId", userId);
        response.put("message", "User joined the conversation");
        return response;
    }

    @MessageMapping("/chat.leave")
    @SendTo("/topic/chat")
    public Map<String, Object> leaveRoom(@Payload Map<String, String> payload, Principal principal) {
        Map<String, Object> response = new HashMap<>();
        if (principal == null) {
            System.err.println(" Principal is null in leaveRoom");
            response.put("error", "Authentication required");
            return response;
        }
        String conversationId = payload.get("conversationId");
        String userId = principal.getName();
        response.put("type", "USER_LEFT");
        response.put("conversationId", conversationId);
        response.put("userId", userId);
        response.put("message", "User left the conversation");
        return response;
    }

    @MessageMapping("/chat.typing")
    @SendTo("/topic/chat/typing")
    public Map<String, Object> userTyping(@Payload Map<String, String> payload, Principal principal, Message<?> message) {
        if (principal == null) {
            SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.wrap(message);
            principal = (Principal) accessor.getHeader("user");
            if (principal == null) {
                principal = (Principal) accessor.getHeader("simpUser");
            }
            if (principal == null) {
                System.err.println(" Principal is still null in userTyping");
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Authentication required");
                return errorResponse;
            }
        }
        
        String conversationId = payload.get("conversationId");
        String userId = principal.getName();
        boolean isTyping = Boolean.parseBoolean(payload.get("isTyping"));

        Map<String, Object> response = new HashMap<>();
        response.put("type", "TYPING");
        response.put("conversationId", conversationId);
        response.put("userId", userId);
        response.put("isTyping", isTyping);

        return response;
    }
}
