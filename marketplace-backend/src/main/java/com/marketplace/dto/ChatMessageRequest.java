package com.marketplace.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ChatMessageRequest {
    @NotBlank(message = "Chat room ID is required")
    private String chatRoomId;

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

    // Constructors
    public ChatMessageRequest() {}

    public ChatMessageRequest(String chatRoomId, String content) {
        this.chatRoomId = chatRoomId;
        this.content = content;
    }

    // Getters and Setters
    public String getChatRoomId() { return chatRoomId; }
    public void setChatRoomId(String chatRoomId) { this.chatRoomId = chatRoomId; }

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
}
