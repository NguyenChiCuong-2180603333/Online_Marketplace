package com.marketplace.controller;

import com.marketplace.model.ChatMessage;
import com.marketplace.model.Conversation;
import com.marketplace.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api/chat")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class ChatController {

    @Autowired
    private ChatService chatService;

    /**
     * WebSocket endpoint - Gửi tin nhắn real-time
     */
    @MessageMapping("/send")
    public void sendMessage(@Payload Map<String, Object> message, Principal principal) {
        String senderId = principal.getName(); // Extract from JWT
        String conversationId = (String) message.get("conversationId");
        String content = (String) message.get("content");
        String messageType = (String) message.getOrDefault("messageType", "TEXT");

        chatService.sendMessage(conversationId, senderId, content, messageType);
    }

    /**
     * REST endpoint - Tạo conversation
     */
    @PostMapping("/conversations")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Conversation> createConversation(
            @RequestBody Map<String, String> request) {
        String currentUserId = getCurrentUserId();
        String otherUserId = request.get("otherUserId");
        String productId = request.get("productId");

        Conversation conversation = chatService.getOrCreateConversation(
                currentUserId, otherUserId, productId);

        return ResponseEntity.ok(conversation);
    }

    /**
     * Lấy danh sách conversations
     */
    @GetMapping("/conversations")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<Conversation>> getUserConversations() {
        String userId = getCurrentUserId();
        List<Conversation> conversations = chatService.getUserConversations(userId);
        return ResponseEntity.ok(conversations);
    }

    /**
     * Lấy messages trong conversation
     */
    @GetMapping("/conversations/{conversationId}/messages")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<ChatMessage>> getConversationMessages(
            @PathVariable String conversationId) {
        String userId = getCurrentUserId();
        List<ChatMessage> messages = chatService.getConversationMessages(conversationId, userId);
        return ResponseEntity.ok(messages);
    }

    /**
     * Đánh dấu đã đọc
     */
    @PutMapping("/conversations/{conversationId}/read")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> markAsRead(@PathVariable String conversationId) {
        String userId = getCurrentUserId();
        chatService.markMessagesAsRead(conversationId, userId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Messages marked as read");
        return ResponseEntity.ok(response);
    }

    /**
     * Đếm tin nhắn chưa đọc
     */
    @GetMapping("/unread-count")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getUnreadCount() {
        String userId = getCurrentUserId();
        long unreadCount = chatService.getUnreadMessageCount(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("unreadCount", unreadCount);
        return ResponseEntity.ok(response);
    }

    /**
     * Xóa tin nhắn
     */
    @DeleteMapping("/messages/{messageId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteMessage(@PathVariable String messageId) {
        String userId = getCurrentUserId();
        chatService.deleteMessage(messageId, userId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Message deleted successfully");
        return ResponseEntity.ok(response);
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // Extract user ID from JWT token
        return "currentUserId"; // Replace with actual implementation
    }
}