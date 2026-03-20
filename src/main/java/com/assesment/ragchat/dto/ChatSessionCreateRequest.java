package com.assesment.ragchat.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class ChatSessionCreateRequest {

    @NotBlank(message = "Session name is required")
    private String name;

    @NotBlank(message = "User name is required")
    private String userName;
}