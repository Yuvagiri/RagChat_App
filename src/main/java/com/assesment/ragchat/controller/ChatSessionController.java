package com.assesment.ragchat.controller;

import com.assesment.ragchat.dto.ChatSessionCreateRequest;
import com.assesment.ragchat.dto.ChatSessionRenameRequest;
import com.assesment.ragchat.entity.ChatSession;
import com.assesment.ragchat.service.ChatSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/rag_chat_storage/sessions")
@RequiredArgsConstructor
@Slf4j
public class ChatSessionController {

    private final ChatSessionService sessionService;

    @GetMapping("/getAll")
    public ResponseEntity<List<ChatSession>> retrieveAllSessions(
            @RequestParam(required = false) String userName) {
        log.info("Request to retrieve all sessions. Filtered by userName: {}", userName != null ? userName : "None");
        List<ChatSession> sessions;
        if (userName != null && !userName.isBlank()) {
            sessions = sessionService.getSessionsByUserName(userName);
        } else {
            sessions = sessionService.getAllSessions();
        }
        log.info("Successfully retrieved {} chat sessions.", sessions.size());
        return ResponseEntity.ok(sessions);
    }

    @PostMapping("/create")
    public ResponseEntity<ChatSession> createSession(
                @Valid @RequestBody ChatSessionCreateRequest request) {
        log.info("Request to create new session for user: {}", request.getUserName());
        ChatSession savedSession = sessionService.createSession(request.getName(),request.getUserName());
        log.info("Successfully created new session with ID: {}", savedSession.getId());
        return ResponseEntity.ok(savedSession);
    }

    @PatchMapping("/{id}/rename")
    public ResponseEntity<ChatSession> renameSession(
            @PathVariable Long id,
            @Valid @RequestBody ChatSessionRenameRequest request) {
        log.info("Request to rename session ID: {} to new name: {}", id, request.getName());
        ChatSession renamedSession = sessionService.renameSession(id, request.getName());
        log.info("Successfully renamed session ID: {}", id);
        return ResponseEntity.ok(renamedSession);
    }

    @PatchMapping("/{id}/favorite")
    public ResponseEntity<ChatSession> markSessionAsFavorite(
            @PathVariable Long id,
            @RequestParam(required = true, defaultValue = "false")Boolean isFavorite) {
        log.info("Request to mark session ID: {} as favorite status: {}", id, isFavorite);
        ChatSession updatedSession = sessionService.markSessionAsFavorite(id, isFavorite);
        log.info("Successfully updated favorite status for session ID: {}", id);
        return ResponseEntity.ok(updatedSession);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteSession(@PathVariable Long id) {
        log.warn("Request to delete session ID: {}", id);
        sessionService.deleteSession(id);
        log.info("Successfully deleted session ID: {}", id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}


