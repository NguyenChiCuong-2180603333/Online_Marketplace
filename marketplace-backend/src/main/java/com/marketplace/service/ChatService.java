package com.marketplace.service;

import com.marketplace.model.ChatRoom;
import com.marketplace.model.ChatMessage;
import com.marketplace.model.User;
import com.marketplace.model.Product;
import com.marketplace.repository.ChatRoomRepository;
import com.marketplace.repository.ChatMessageRepository;
import com.marketplace.dto.ChatMessageRequest;
import com.marketplace.dto.ChatRoomRequest;
import com.marketplace.exception.ResourceNotFoundException;
import com.marketplace.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ChatService {

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // Tạo hoặc lấy chat room giữa buyer và seller
    public ChatRoom getOrCreateProductChatRoom(String buyerId, String sellerId, String productId) {
        // Kiểm tra room đã tồn tại chưa
        Optional<ChatRoom> existingRoom = chatRoomRepository
                .findBySellerIdAndBuyerIdAndProductId(sellerId, buyerId, productId);

        if (existingRoom.isPresent()) {
            return existingRoom.get();
        }

        // Validate users và product
        User buyer = userService.getUserById(buyerId);
        User seller = userService.getUserById(sellerId);
        Product product = productService.getProductById(productId);

        // Tạo room mới
        ChatRoom chatRoom = new ChatRoom(sellerId, buyerId, productId);
        chatRoom.setName("Chat về: " + product.getName());
        chatRoom.setRoomType("PRODUCT_INQUIRY");

        return chatRoomRepository.save(chatRoom);
    }

    // Lấy tất cả chat rooms của user
    public List<ChatRoom> getUserChatRooms(String userId) {
        List<ChatRoom> rooms = chatRoomRepository.findByParticipantsContainingAndActiveTrue(userId);

        // Sắp xếp theo tin nhắn cuối cùng
        return rooms.stream()
                .sorted((r1, r2) -> {
                    LocalDateTime last1 = r1.getLastMessageAt() != null ? r1.getLastMessageAt() : r1.getCreatedAt();
                    LocalDateTime last2 = r2.getLastMessageAt() != null ? r2.getLastMessageAt() : r2.getCreatedAt();
                    return last2.compareTo(last1);
                })
                .collect(Collectors.toList());
    }

    // Lấy chi tiết chat room với messages
    public Map<String, Object> getChatRoomDetails(String roomId, String userId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat room not found"));

        // Kiểm tra quyền truy cập
        if (!room.getParticipants().contains(userId)) {
            throw new BadRequestException("Bạn không có quyền truy cập room này");
        }

        // Lấy messages
        List<ChatMessage> messages = chatMessageRepository.findByChatRoomIdOrderByCreatedAtAsc(roomId);

        // Đánh dấu messages đã đọc
        markMessagesAsRead(roomId, userId);

        Map<String, Object> result = new HashMap<>();
        result.put("room", room);
        result.put("messages", messages);
        result.put("totalMessages", messages.size());

        return result;
    }

    // Gửi tin nhắn
    public ChatMessage sendMessage(ChatMessageRequest request, String senderId) {
        ChatRoom room = chatRoomRepository.findById(request.getChatRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Chat room not found"));

        // Kiểm tra quyền gửi tin nhắn
        if (!room.getParticipants().contains(senderId)) {
            throw new BadRequestException("Bạn không có quyền gửi tin nhắn trong room này");
        }

        User sender = userService.getUserById(senderId);

        // Tạo message
        ChatMessage message = new ChatMessage();
        message.setChatRoomId(request.getChatRoomId());
        message.setSenderId(senderId);
        message.setSenderName(sender.getFirstName() + " " + sender.getLastName());
        message.setSenderAvatar(sender.getAvatar());
        message.setContent(request.getContent());
        message.setMessageType(request.getMessageType());
        message.setAttachmentUrl(request.getAttachmentUrl());
        message.setAttachmentName(request.getAttachmentName());
        message.setRelatedProductId(request.getRelatedProductId());
        message.setRelatedOrderId(request.getRelatedOrderId());

        ChatMessage savedMessage = chatMessageRepository.save(message);

        // Cập nhật last message time của room
        room.setLastMessageAt(LocalDateTime.now());
        room.setUpdatedAt(LocalDateTime.now());
        chatRoomRepository.save(room);

        // Gửi real-time notification
        sendRealTimeMessage(savedMessage, room);

        return savedMessage;
    }

    // Đánh dấu tin nhắn đã đọc
    public void markMessagesAsRead(String roomId, String userId) {
        List<ChatMessage> unreadMessages = chatMessageRepository
                .findUnreadMessagesByRoomAndUser(roomId, userId);

        for (ChatMessage message : unreadMessages) {
            message.setRead(true);
            message.setReadAt(LocalDateTime.now());
        }

        if (!unreadMessages.isEmpty()) {
            chatMessageRepository.saveAll(unreadMessages);

            // Notify other participants about read status
            notifyMessageRead(roomId, userId, unreadMessages.size());
        }
    }

    // Chỉnh sửa tin nhắn
    public ChatMessage editMessage(String messageId, String newContent, String userId) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));

        // Chỉ sender mới được edit
        if (!message.getSenderId().equals(userId)) {
            throw new BadRequestException("Bạn chỉ có thể chỉnh sửa tin nhắn của chính mình");
        }

        // Không cho edit sau 5 phút
        if (message.getCreatedAt().plusMinutes(5).isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Không thể chỉnh sửa tin nhắn sau 5 phút");
        }

        message.setContent(newContent);
        message.setEdited(true);
        message.setEditedAt(LocalDateTime.now());

        ChatMessage updatedMessage = chatMessageRepository.save(message);

        // Notify real-time update
        notifyMessageEdit(updatedMessage);

        return updatedMessage;
    }

    // Xóa tin nhắn
    public void deleteMessage(String messageId, String userId) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));

        // Chỉ sender mới được xóa
        if (!message.getSenderId().equals(userId)) {
            throw new BadRequestException("Bạn chỉ có thể xóa tin nhắn của chính mình");
        }

        chatMessageRepository.delete(message);

        // Notify deletion
        notifyMessageDelete(message.getChatRoomId(), messageId);
    }

    // Đóng chat room
    public void closeChatRoom(String roomId, String userId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat room not found"));

        // Chỉ participants mới được đóng
        if (!room.getParticipants().contains(userId)) {
            throw new BadRequestException("Bạn không có quyền đóng room này");
        }

        room.setActive(false);
        room.setUpdatedAt(LocalDateTime.now());
        chatRoomRepository.save(room);

        // Notify participants
        notifyRoomClosed(room);
    }

    // Lấy số tin nhắn chưa đọc của user
    public long getUnreadCount(String userId) {
        List<ChatRoom> userRooms = chatRoomRepository.findByParticipantsContainingAndActiveTrue(userId);

        return userRooms.stream()
                .mapToLong(room -> chatMessageRepository.countUnreadMessagesByRoomAndUser(room.getId(), userId))
                .sum();
    }

    // Tìm kiếm tin nhắn
    public List<ChatMessage> searchMessages(String roomId, String query, String userId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat room not found"));

        if (!room.getParticipants().contains(userId)) {
            throw new BadRequestException("Bạn không có quyền tìm kiếm trong room này");
        }

        List<ChatMessage> allMessages = chatMessageRepository.findByChatRoomIdOrderByCreatedAtDesc(roomId);

        return allMessages.stream()
                .filter(message -> message.getContent().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
    }

    // Real-time messaging methods
    private void sendRealTimeMessage(ChatMessage message, ChatRoom room) {
        // Gửi đến tất cả participants trong room
        for (String participantId : room.getParticipants()) {
            if (!participantId.equals(message.getSenderId())) {
                messagingTemplate.convertAndSendToUser(
                        participantId,
                        "/topic/chat/" + room.getId(),
                        message
                );
            }
        }
    }

    private void notifyMessageRead(String roomId, String userId, int messageCount) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "MESSAGES_READ");
        notification.put("roomId", roomId);
        notification.put("userId", userId);
        notification.put("count", messageCount);

        messagingTemplate.convertAndSend("/topic/chat/" + roomId + "/read", notification);
    }

    private void notifyMessageEdit(ChatMessage message) {
        messagingTemplate.convertAndSend(
                "/topic/chat/" + message.getChatRoomId() + "/edit",
                message
        );
    }

    private void notifyMessageDelete(String roomId, String messageId) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "MESSAGE_DELETED");
        notification.put("messageId", messageId);

        messagingTemplate.convertAndSend("/topic/chat/" + roomId + "/delete", notification);
    }

    private void notifyRoomClosed(ChatRoom room) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "ROOM_CLOSED");
        notification.put("roomId", room.getId());

        for (String participantId : room.getParticipants()) {
            messagingTemplate.convertAndSendToUser(
                    participantId,
                    "/topic/chat/rooms",
                    notification
            );
        }
    }
}