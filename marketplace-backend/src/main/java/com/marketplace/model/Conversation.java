package com.marketplace.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "conversations")
public class Conversation {
    @Id
    private String id;

    @NotNull(message = "Participants are required")
    private List<String> participantIds;

    private String productId; // Optional: conversation about a specific product
    private String lastMessageId;
    private String lastMessageContent;
    private LocalDateTime lastMessageTime;
    private LocalDateTime createdAt = LocalDateTime.now();
    private boolean isActive = true;

    // Constructors
    public Conversation() {}

    public Conversation(List<String> participantIds) {
        this.participantIds = participantIds;
    }

    public Conversation(List<String> participantIds, String productId) {
        this.participantIds = participantIds;
        this.productId = productId;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public List<String> getParticipantIds() { return participantIds; }
    public void setParticipantIds(List<String> participantIds) { this.participantIds = participantIds; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getLastMessageId() { return lastMessageId; }
    public void setLastMessageId(String lastMessageId) { this.lastMessageId = lastMessageId; }

    public String getLastMessageContent() { return lastMessageContent; }
    public void setLastMessageContent(String lastMessageContent) { this.lastMessageContent = lastMessageContent; }

    public LocalDateTime getLastMessageTime() { return lastMessageTime; }
    public void setLastMessageTime(LocalDateTime lastMessageTime) { this.lastMessageTime = lastMessageTime; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}