package com.assesment.ragchat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MessageCreateRequest {

    @NotNull(message = "sessionId is required")
    private Long sessionId;

    private Boolean isBot;

    @NotBlank(message = "message is required")
    private String content;

    private String context;
}
