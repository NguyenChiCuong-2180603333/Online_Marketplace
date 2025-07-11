package com.marketplace.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.List;

public class ChatRoomRequest {
    @NotBlank(message = "Room name is required")
    private String name;

    @NotNull(message = "Participants list is required")
    private List<String> participants;

    private String productId;

    @Pattern(regexp = "PRODUCT_INQUIRY|SUPPORT|GROUP",
            message = "Room type must be PRODUCT_INQUIRY, SUPPORT, or GROUP")
    private String roomType = "PRODUCT_INQUIRY";

    public ChatRoomRequest() {}

    public ChatRoomRequest(String name, List<String> participants) {
        this.name = name;
        this.participants = participants;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<String> getParticipants() { return participants; }
    public void setParticipants(List<String> participants) { this.participants = participants; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }
}