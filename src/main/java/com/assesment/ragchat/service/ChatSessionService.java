package com.assesment.ragchat.service;

import com.assesment.ragchat.entity.ChatSession;
import com.assesment.ragchat.exception.ResourceNotFoundException;
import com.assesment.ragchat.repo.ChatSessionRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatSessionService {

    private final ChatSessionRepository repo;

    public ChatSession createSession(@NotBlank(message = "Session Name is required") String name, @NotBlank(message = "User Id is required") String userName) {
        log.info("Creating new session with name '{}' for user '{}'", name, userName);
        ChatSession session = ChatSession.builder()
                .name(name)
                .userName(userName)
                .build();
        ChatSession savedSession = repo.save(session);
        log.info("Session created successfully with ID: {}", savedSession.getId());
        return savedSession;
    }

    public List<ChatSession> getAllSessions() {
        log.info("Retrieving all chat sessions from the repository.");
        List<ChatSession> chatSessionList = repo.findAll();
        log.info("Found {} total chat sessions.", chatSessionList.size());
        return chatSessionList;
    }

    @Transactional
    public ChatSession renameSession(Long id, @NotBlank(message = "Session name is required") String newName) {
        log.info("Attempting to rename session ID {} to '{}'", id, newName);
        ChatSession session = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Chat Session not found with ID: " + id));
        if (newName == null || newName.isBlank()) {
            log.warn("Rename failed for session ID {}: new name is blank.", id);
            throw new IllegalArgumentException("Session name cannot be blank.");
        }
        session.setName(newName);
        ChatSession renamedSession = repo.save(session);
        log.info("Successfully renamed session ID {} to '{}'.", id, newName);
        return renamedSession;
    }

    @Transactional
    public void deleteSession(Long id) {
        log.warn("Attempting to delete chat session ID: {}", id);
        if (!repo.existsById(id)) {
            log.error("Deletion failed: ChatSession not found with ID: {}", id);
            throw new ResourceNotFoundException("ChatSession", "id", id);
        }
        repo.deleteById(id);
        log.info("Successfully deleted chat session ID: {}", id);
    }

    public ChatSession markSessionAsFavorite(Long id, Boolean isFavorite) {
        log.info("Updating favorite status for session ID {} to: {}", id, isFavorite);
        ChatSession session = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Chat Session not found with ID: " + id));
        session.setFavourite(isFavorite);
        ChatSession updatedSession = repo.save(session);
        log.info("Favorite status successfully updated for session ID {}", id);
        return updatedSession;
    }

    public List<ChatSession> getSessionsByUserName(String userName) {
        log.info("Retrieving sessions from database filtered by userName: {}", userName);
        List<ChatSession> sessions = repo.findByUserName(userName);
        log.info("Retrieved {} sessions for user: {}", sessions.size(), userName);
        return sessions;
    }

}