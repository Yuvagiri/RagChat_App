package com.assesment.ragchat.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatSessionRenameRequest {

    @NotBlank(message = "New Session name is required to rename")
    private String name;
}
