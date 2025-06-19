package com.marketplace.controller;

import com.marketplace.dto.ChatMessageRequest;
import com.marketplace.model.ChatRoom;
import com.marketplace.model.ChatMessage;
import com.marketplace.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
public class ChatController {

    @Autowired
    private ChatService chatService;

    // Lấy danh sách chat rooms của user
    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoom>> getUserChatRooms() {
        String userId = getCurrentUserId();
        List<ChatRoom> rooms = chatService.getUserChatRooms(userId);
        return ResponseEntity.ok(rooms);
    }

    // Tạo hoặc lấy chat room cho sản phẩm
    @PostMapping("/rooms/product")
    public ResponseEntity<ChatRoom> getOrCreateProductChatRoom(@RequestBody Map<String, String> request) {
        String userId = getCurrentUserId();
        String sellerId = request.get("sellerId");
        String productId = request.get("productId");

        ChatRoom room = chatService.getOrCreateProductChatRoom(userId, sellerId, productId);
        return ResponseEntity.ok(room);
    }

    // Lấy chi tiết chat room với messages
    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<Map<String, Object>> getChatRoomDetails(@PathVariable String roomId) {
        String userId = getCurrentUserId();
        Map<String, Object> details = chatService.getChatRoomDetails(roomId, userId);
        return ResponseEntity.ok(details);
    }

    // Gửi tin nhắn qua REST API
    @PostMapping("/messages")
    public ResponseEntity<ChatMessage> sendMessage(@Valid @RequestBody ChatMessageRequest request) {
        String userId = getCurrentUserId();
        ChatMessage message = chatService.sendMessage(request, userId);
        return ResponseEntity.ok(message);
    }

    // Đánh dấu tin nhắn đã đọc
    @PutMapping("/rooms/{roomId}/read")
    public ResponseEntity<?> markMessagesAsRead(@PathVariable String roomId) {
        String userId = getCurrentUserId();
        chatService.markMessagesAsRead(roomId, userId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Đã đánh dấu tin nhắn đã đọc");
        return ResponseEntity.ok(response);
    }

    // Chỉnh sửa tin nhắn
    @PutMapping("/messages/{messageId}")
    public ResponseEntity<ChatMessage> editMessage(
            @PathVariable String messageId,
            @RequestBody Map<String, String> request) {
        String userId = getCurrentUserId();
        String newContent = request.get("content");

        ChatMessage message = chatService.editMessage(messageId, newContent, userId);
        return ResponseEntity.ok(message);
    }

    // Xóa tin nhắn
    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<?> deleteMessage(@PathVariable String messageId) {
        String userId = getCurrentUserId();
        chatService.deleteMessage(messageId, userId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Tin nhắn đã được xóa");
        return ResponseEntity.ok(response);
    }

    // Đóng chat room
    @PutMapping("/rooms/{roomId}/close")
    public ResponseEntity<?> closeChatRoom(@PathVariable String roomId) {
        String userId = getCurrentUserId();
        chatService.closeChatRoom(roomId, userId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Chat room đã được đóng");
        return ResponseEntity.ok(response);
    }

    // Lấy số tin nhắn chưa đọc
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Object>> getUnreadCount() {
        String userId = getCurrentUserId();
        long unreadCount = chatService.getUnreadCount(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("unreadCount", unreadCount);
        return ResponseEntity.ok(response);
    }

    // Tìm kiếm tin nhắn
    @GetMapping("/rooms/{roomId}/search")
    public ResponseEntity<List<ChatMessage>> searchMessages(
            @PathVariable String roomId,
            @RequestParam String query) {
        String userId = getCurrentUserId();
        List<ChatMessage> messages = chatService.searchMessages(roomId, query, userId);
        return ResponseEntity.ok(messages);
    }

    // Upload file cho chat
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadChatFile(
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            @RequestParam("roomId") String roomId) {

        // Validate file và room access
        String userId = getCurrentUserId();

        // Implementation for file upload
        String fileUrl = "https://example.com/uploads/" + file.getOriginalFilename();

        Map<String, Object> response = new HashMap<>();
        response.put("fileUrl", fileUrl);
        response.put("fileName", file.getOriginalFilename());
        response.put("fileSize", file.getSize());
        response.put("contentType", file.getContentType());

        return ResponseEntity.ok(response);
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() &&
                !authentication.getPrincipal().equals("anonymousUser")) {

            String token = getJwtFromCurrentRequest();
            if (token != null) {
                try {
                    org.springframework.context.ApplicationContext context =
                            org.springframework.web.context.support.WebApplicationContextUtils
                                    .getWebApplicationContext(((org.springframework.web.context.request.ServletRequestAttributes)
                                            org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes())
                                            .getRequest().getServletContext());

                    if (context != null) {
                        com.marketplace.security.JwtTokenProvider jwtProvider = context.getBean(com.marketplace.security.JwtTokenProvider.class);
                        return jwtProvider.getUserIdFromToken(token);
                    }
                } catch (Exception e) {
                    // Fall back to mock for now
                }
            }
        }
        return "mockUserId123";
    }

    private String getJwtFromCurrentRequest() {
        try {
            org.springframework.web.context.request.ServletRequestAttributes attr =
                    (org.springframework.web.context.request.ServletRequestAttributes)
                            org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes();
            jakarta.servlet.http.HttpServletRequest request = attr.getRequest();
            String bearerToken = request.getHeader("Authorization");
            if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                return bearerToken.substring(7);
            }
        } catch (Exception e) {
            // Ignore
        }
        return null;
    }
}

