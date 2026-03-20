package com.assesment.ragchat.service;

import com.assesment.ragchat.entity.ChatSession;
import com.assesment.ragchat.entity.Message;
import com.assesment.ragchat.repo.ChatSessionRepository;
import com.assesment.ragchat.repo.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {

    private final MessageRepository messageRepository;
    private final ChatSessionRepository chatSessionRepository;

    public Message createMessage(Long sessionId, boolean bot, String content, String context) {
        log.info("Attempting to create new message for session ID: {}", sessionId);
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid session ID: " + sessionId));

        Message message = Message.builder()
                .chatSession(session)
                .isBot(bot)
                .content(content)
                .context(context)
                .build();
        Message savedMessage = messageRepository.save(message);
        log.info("Message created successfully with ID: {}", savedMessage.getId());
        return savedMessage;
    }

    public List<Message> getMessagesBySession(Long sessionId) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> {
                    log.error("Retrieval failed: ChatSession not found with ID: {}", sessionId);
                    return new IllegalArgumentException("Invalid session ID: " + sessionId);
                });
        List<Message> messages = messageRepository.findByChatSession_IdOrderByCreatedAtAsc(sessionId);
        log.info("Successfully retrieved {} messages for session ID: {}", messages.size(), sessionId);
        return messages;
    }
}
