package com.marketplace.controller;

import com.marketplace.dto.ChatMessageRequest;
import com.marketplace.model.ChatMessage;
import com.marketplace.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

// WebSocket Controller cho real-time messaging
@Controller
public class ChatWebSocketController {

    @Autowired
    private ChatService chatService;

    // Gửi tin nhắn real-time
    @MessageMapping("/chat.send")
    @SendTo("/topic/chat")
    public ChatMessage sendMessage(@Payload ChatMessageRequest request, Principal principal) {
        String userId = principal.getName(); // Get user ID from principal
    
        String conversationId = request.getConversationId();
        String content = request.getContent();
        String messageType = request.getMessageType() != null ? request.getMessageType() : "TEXT";
    
        return chatService.sendMessage(conversationId, userId, content, messageType);
    }

    // Join chat room
    @MessageMapping("/chat.join")
    @SendTo("/topic/chat")
    public Map<String, Object> joinRoom(@Payload Map<String, String> payload, Principal principal) {
        String roomId = payload.get("roomId");
        String userId = principal.getName();

        Map<String, Object> response = new HashMap<>();
        response.put("type", "USER_JOINED");
        response.put("roomId", roomId);
        response.put("userId", userId);
        response.put("message", "User joined the room");

        return response;
    }

    // Leave chat room
    @MessageMapping("/chat.leave")
    @SendTo("/topic/chat")
    public Map<String, Object> leaveRoom(@Payload Map<String, String> payload, Principal principal) {
        String roomId = payload.get("roomId");
        String userId = principal.getName();

        Map<String, Object> response = new HashMap<>();
        response.put("type", "USER_LEFT");
        response.put("roomId", roomId);
        response.put("userId", userId);
        response.put("message", "User left the room");

        return response;
    }

    // Typing indicator
    @MessageMapping("/chat.typing")
    @SendTo("/topic/chat/typing")
    public Map<String, Object> userTyping(@Payload Map<String, String> payload, Principal principal) {
        String roomId = payload.get("roomId");
        String userId = principal.getName();
        boolean isTyping = Boolean.parseBoolean(payload.get("isTyping"));

        Map<String, Object> response = new HashMap<>();
        response.put("type", "TYPING");
        response.put("roomId", roomId);
        response.put("userId", userId);
        response.put("isTyping", isTyping);

        return response;
    }
}
