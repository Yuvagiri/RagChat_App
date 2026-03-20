package com.assesment.ragchat.controller;

import com.assesment.ragchat.dto.MessageCreateRequest;
import com.assesment.ragchat.entity.Message;
import com.assesment.ragchat.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rag_chat_storage/messages")
@RequiredArgsConstructor
@Slf4j
public class MessageController {

    private final MessageService messageService;

    @PostMapping
    public ResponseEntity<Message> createMessage(
            @Valid @RequestBody MessageCreateRequest request) {
        log.info("Starting message creation for session: {}", request.getSessionId());
        Message saved = messageService.createMessage(request.getSessionId(), request.getIsBot(), request.getContent(),
                request.getContext() );
        log.info("Message successfully created with database ID: {}", saved.getId());
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<List<Message>> getMessagesBySession(@PathVariable Long sessionId) {
        log.info("Request to retrieve messages for session ID: {}", sessionId);
        List<Message> messages = messageService.getMessagesBySession(sessionId);
        log.info("Successfully retrieved {} messages for session ID: {}", messages.size(), sessionId);
        return ResponseEntity.ok(messages);
    }

}
