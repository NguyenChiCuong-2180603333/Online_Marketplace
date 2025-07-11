package com.marketplace.controller;

import com.marketplace.model.ChatMessage;
import com.marketplace.model.Conversation;
import com.marketplace.service.ChatService;
import com.marketplace.service.ProductService;
import com.marketplace.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.marketplace.repository.ConversationRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.security.Principal;

@Controller
@RequestMapping("/api/chat")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ConversationRepository conversationRepository;

   
    @PostMapping("/conversations")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Conversation> createConversation(
            @RequestBody Map<String, String> request) {
        String currentUserId = getCurrentUserId();
        String productId = request.get("productId");
        String otherUserId = request.get("otherUserId");
        String participant2Id = null;
        if (productId != null && !productId.isEmpty()) {
            // Lấy sellerId từ product
            participant2Id = productService.getProductById(productId).getSellerId();
        } else if (otherUserId != null && !otherUserId.isEmpty()) {
            participant2Id = otherUserId;
        } else {
            throw new RuntimeException("Missing participant information");
        }
        if (participant2Id.equals(currentUserId)) {
            throw new RuntimeException("Cannot create conversation with yourself");
        }
        Conversation conversation = chatService.getOrCreateConversation(
                currentUserId, participant2Id, productId);
        return ResponseEntity.ok(conversation);
    }

   
    @GetMapping("/conversations")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getUserConversations() {
        String userId = getCurrentUserId();
        List<Conversation> conversations = chatService.getUserConversations(userId);
        // Trả về DTO có trường unreadCount cho user hiện tại
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (Conversation conv : conversations) {
            Map<String, Object> dto = new java.util.HashMap<>();
            dto.put("id", conv.getId());
            dto.put("participantIds", conv.getParticipantIds());
            dto.put("productId", conv.getProductId());
            dto.put("lastMessageId", conv.getLastMessageId());
            dto.put("lastMessageContent", conv.getLastMessageContent());
            dto.put("lastMessageTime", conv.getLastMessageTime());
            dto.put("createdAt", conv.getCreatedAt());
            dto.put("isActive", conv.isActive());
            // Thêm unreadCount cho user hiện tại
            int unread = conv.getUnreadCount() != null ? conv.getUnreadCount().getOrDefault(userId, 0) : 0;
            dto.put("unreadCount", unread);
            result.add(dto);
        }
        return ResponseEntity.ok(result);
    }

  
    @GetMapping("/conversations/{conversationId}/messages")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getConversationMessages(
            @PathVariable String conversationId) {
        String userId = getCurrentUserId();
        List<ChatMessage> messages = chatService.getConversationMessages(conversationId, userId);
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (ChatMessage msg : messages) {
            Map<String, Object> dto = new java.util.HashMap<>();
            dto.put("id", msg.getId());
            dto.put("conversationId", msg.getConversationId());
            dto.put("senderId", msg.getSenderId());
            dto.put("receiverId", msg.getReceiverId());
            dto.put("content", msg.getContent());
            dto.put("messageType", msg.getMessageType());
            dto.put("attachmentUrl", msg.getAttachmentUrl());
            dto.put("isRead", msg.isRead());
            dto.put("isEdited", msg.isEdited());
            dto.put("createdAt", msg.getCreatedAt());
            dto.put("editedAt", msg.getEditedAt());
            // Thêm senderName, senderAvatar
            try {
                var sender = userService.getUserById(msg.getSenderId());
                dto.put("senderName", (sender.getFirstName() != null ? sender.getFirstName() : "") + " " + (sender.getLastName() != null ? sender.getLastName() : ""));
                dto.put("senderAvatar", sender.getAvatar());
            } catch (Exception e) {
                dto.put("senderName", "Người dùng");
                dto.put("senderAvatar", "/placeholder-avatar.jpg");
            }
            result.add(dto);
        }
        return ResponseEntity.ok(result);
    }

  
    @PutMapping("/conversations/{conversationId}/read")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> markAsRead(@PathVariable String conversationId) {
        String userId = getCurrentUserId();
        chatService.markMessagesAsRead(conversationId, userId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Messages marked as read");
        return ResponseEntity.ok(response);
    }

 
    @GetMapping("/unread-count")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getUnreadCount() {
        String userId = getCurrentUserId();
        long unreadCount = chatService.getUnreadMessageCount(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("unreadCount", unreadCount);
        return ResponseEntity.ok(response);
    }

   
    @DeleteMapping("/messages/{messageId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteMessage(@PathVariable String messageId) {
        String userId = getCurrentUserId();
        chatService.deleteMessage(messageId, userId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Message deleted successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<ChatMessage> sendMessage(
            @PathVariable String conversationId,
            @RequestBody Map<String, String> body,
            Principal principal) {
        String content = body.get("content");
        String messageType = body.getOrDefault("messageType", "TEXT");
        String userId = principal.getName();
        ChatMessage saved = chatService.sendMessage(conversationId, userId, content, messageType);

        Conversation conversation = conversationRepository.findById(conversationId).orElse(null);
        if (conversation != null) {
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("id", saved.getId());
            messageData.put("conversationId", saved.getConversationId());
            messageData.put("senderId", saved.getSenderId());
            messageData.put("receiverId", saved.getReceiverId());
            messageData.put("content", saved.getContent());
            messageData.put("messageType", saved.getMessageType());
            messageData.put("attachmentUrl", saved.getAttachmentUrl());
            messageData.put("isRead", saved.isRead());
            messageData.put("isEdited", saved.isEdited());
            messageData.put("createdAt", saved.getCreatedAt());
            messageData.put("editedAt", saved.getEditedAt());
            for (String participantId : conversation.getParticipantIds()) {
                System.out.println("[WebSocket] Broadcast to user: " + participantId + " data: " + messageData);
                messagingTemplate.convertAndSendToUser(
                    participantId,
                    "/queue/messages",
                    messageData
                );
            }
            messagingTemplate.convertAndSend("/topic/chat", messageData);
        }
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/conversations/{conversationId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteConversation(@PathVariable String conversationId) {
        String userId = getCurrentUserId();
        chatService.deleteConversation(conversationId, userId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Conversation deleted successfully");
        return ResponseEntity.ok(response);
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        throw new RuntimeException("User not authenticated");
    }

    @GetMapping("/test-broadcast")
public ResponseEntity<String> testBroadcast() {
    Map<String, Object> msg = new HashMap<>();
    msg.put("test", "hello from backend");
    messagingTemplate.convertAndSend("/topic/chat", msg);
    return ResponseEntity.ok("Broadcasted test message");
}
}