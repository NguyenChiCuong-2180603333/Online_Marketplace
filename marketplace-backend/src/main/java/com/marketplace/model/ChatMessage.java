package com.marketplace.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

@Document(collection = "chatmessages")
public class ChatMessage {
    @Id
    private String id;

    @NotBlank(message = "Chat room ID is required")
    private String chatRoomId;

    @NotBlank(message = "Sender ID is required")
    private String senderId;

    @NotBlank(message = "Sender name is required")
    private String senderName;

    private String senderAvatar;

    @NotBlank(message = "Message content is required")
    @Size(max = 1000, message = "Message must not exceed 1000 characters")
    private String content;

    @Pattern(regexp = "TEXT|IMAGE|FILE|PRODUCT_LINK|ORDER_UPDATE",
            message = "Message type must be TEXT, IMAGE, FILE, PRODUCT_LINK, or ORDER_UPDATE")
    private String messageType = "TEXT";

    private String attachmentUrl;
    private String attachmentName;
    private String relatedProductId;
    private String relatedOrderId;

    private boolean isRead = false;
    private boolean isEdited = false;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime editedAt;
    private LocalDateTime readAt;

    // Constructors
    public ChatMessage() {}

    public ChatMessage(String chatRoomId, String senderId, String senderName, String content) {
        this.chatRoomId = chatRoomId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.content = content;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getChatRoomId() { return chatRoomId; }
    public void setChatRoomId(String chatRoomId) { this.chatRoomId = chatRoomId; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getSenderAvatar() { return senderAvatar; }
    public void setSenderAvatar(String senderAvatar) { this.senderAvatar = senderAvatar; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }

    public String getAttachmentUrl() { return attachmentUrl; }
    public void setAttachmentUrl(String attachmentUrl) { this.attachmentUrl = attachmentUrl; }

    public String getAttachmentName() { return attachmentName; }
    public void setAttachmentName(String attachmentName) { this.attachmentName = attachmentName; }

    public String getRelatedProductId() { return relatedProductId; }
    public void setRelatedProductId(String relatedProductId) { this.relatedProductId = relatedProductId; }

    public String getRelatedOrderId() { return relatedOrderId; }
    public void setRelatedOrderId(String relatedOrderId) { this.relatedOrderId = relatedOrderId; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public boolean isEdited() { return isEdited; }
    public void setEdited(boolean edited) { isEdited = edited; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getEditedAt() { return editedAt; }
    public void setEditedAt(LocalDateTime editedAt) { this.editedAt = editedAt; }

    public LocalDateTime getReadAt() { return readAt; }
    public void setReadAt(LocalDateTime readAt) { this.readAt = readAt; }
}