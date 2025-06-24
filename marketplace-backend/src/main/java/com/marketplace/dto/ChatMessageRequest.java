package com.marketplace.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ChatMessageRequest {
    
    @NotNull(message = "Conversation ID không được null")
    private String conversationId;
    
    @NotBlank(message = "Nội dung tin nhắn không được trống")
    private String content;
    
    private String messageType = "TEXT"; 
    private String receiverId;
    private String productId; 
    private String metadata; 
    
    public ChatMessageRequest() {}
    
    public ChatMessageRequest(String conversationId, String content) {
        this.conversationId = conversationId;
        this.content = content;
    }
    
    public ChatMessageRequest(String conversationId, String content, String messageType) {
        this.conversationId = conversationId;
        this.content = content;
        this.messageType = messageType;
    }
    
    public String getConversationId() {
        return conversationId;
    }
    
    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getMessageType() {
        return messageType;
    }
    
    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }
    
    public String getReceiverId() {
        return receiverId;
    }
    
    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }
    
    public String getProductId() {
        return productId;
    }
    
    public void setProductId(String productId) {
        this.productId = productId;
    }
    
    public String getMetadata() {
        return metadata;
    }
    
    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }
    
    @Override
    public String toString() {
        return "ChatMessageRequest{" +
                "conversationId='" + conversationId + '\'' +
                ", content='" + content + '\'' +
                ", messageType='" + messageType + '\'' +
                ", receiverId='" + receiverId + '\'' +
                ", productId='" + productId + '\'' +
                ", metadata='" + metadata + '\'' +
                '}';
    }
}