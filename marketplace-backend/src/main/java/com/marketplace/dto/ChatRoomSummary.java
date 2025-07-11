package com.marketplace.dto;

import java.time.LocalDateTime;

public class ChatRoomSummary {
    private String id;
    private String name;
    private String productId;
    private String productName;
    private String productImage;
    private String otherParticipantId;
    private String otherParticipantName;
    private String otherParticipantAvatar;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private long unreadCount;
    private boolean active;

    public ChatRoomSummary() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getProductImage() { return productImage; }
    public void setProductImage(String productImage) { this.productImage = productImage; }

    public String getOtherParticipantId() { return otherParticipantId; }
    public void setOtherParticipantId(String otherParticipantId) { this.otherParticipantId = otherParticipantId; }

    public String getOtherParticipantName() { return otherParticipantName; }
    public void setOtherParticipantName(String otherParticipantName) { this.otherParticipantName = otherParticipantName; }

    public String getOtherParticipantAvatar() { return otherParticipantAvatar; }
    public void setOtherParticipantAvatar(String otherParticipantAvatar) { this.otherParticipantAvatar = otherParticipantAvatar; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public LocalDateTime getLastMessageAt() { return lastMessageAt; }
    public void setLastMessageAt(LocalDateTime lastMessageAt) { this.lastMessageAt = lastMessageAt; }

    public long getUnreadCount() { return unreadCount; }
    public void setUnreadCount(long unreadCount) { this.unreadCount = unreadCount; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}