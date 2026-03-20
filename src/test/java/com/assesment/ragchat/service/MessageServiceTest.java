package com.assesment.ragchat.service;

import com.assesment.ragchat.entity.ChatSession;
import com.assesment.ragchat.entity.Message;
import com.assesment.ragchat.repo.ChatSessionRepository;
import com.assesment.ragchat.repo.MessageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private ChatSessionRepository chatSessionRepository;

    @InjectMocks
    private MessageService messageService;

    private ChatSession createMockSession(Long id) {
        return ChatSession.builder().id(id).name("Test Session").userName("user").build();
    }

    private Message createMockMessage(Long id, ChatSession session, boolean isBot, String content) {
        return Message.builder()
                .id(id)
                .chatSession(session)
                .isBot(isBot)
                .content(content)
                .context(isBot ? "context" : null)
                .build();
    }

    // --- createMessage tests ---

    @Test
    void createMessage_shouldSaveAndReturnMessage() {
        Long sessionId = 1L;
        ChatSession session = createMockSession(sessionId);
        String content = "Hello";
        boolean isBot = false;
        String context = null;
        Message unsavedMessage = createMockMessage(0L, session, isBot, content);
        Message savedMessage = createMockMessage(10L, session, isBot, content);

        when(chatSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);

        Message result = messageService.createMessage(sessionId, isBot, content, context);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getContent()).isEqualTo(content);
        assertThat(result.getChatSession().getId()).isEqualTo(sessionId);
        verify(chatSessionRepository, times(1)).findById(sessionId);
        verify(messageRepository, times(1)).save(any(Message.class));
    }

    @Test
    void createMessage_invalidSessionId_shouldThrowException() {
        Long sessionId = 99L;
        String content = "Hello";

        when(chatSessionRepository.findById(sessionId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                messageService.createMessage(sessionId, false, content, null)
        );

        verify(chatSessionRepository, times(1)).findById(sessionId);
        verify(messageRepository, never()).save(any(Message.class));
    }

    // --- getMessagesBySession tests ---

    @Test
    void getMessagesBySession_existingSession_shouldReturnMessages() {
        Long sessionId = 1L;
        ChatSession session = createMockSession(sessionId);
        Message m1 = createMockMessage(10L, session, false, "User message");
        Message m2 = createMockMessage(11L, session, true, "Bot response");
        List<Message> messages = List.of(m1, m2);

        when(chatSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(messageRepository.findByChatSession_IdOrderByCreatedAtAsc(sessionId)).thenReturn(messages);

        List<Message> result = messageService.getMessagesBySession(sessionId);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getContent()).isEqualTo("User message");
        verify(chatSessionRepository, times(1)).findById(sessionId);
        verify(messageRepository, times(1)).findByChatSession_IdOrderByCreatedAtAsc(sessionId);
    }

    @Test
    void getMessagesBySession_invalidSessionId_shouldThrowException() {
        Long sessionId = 99L;

        when(chatSessionRepository.findById(sessionId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                messageService.getMessagesBySession(sessionId)
        );

        verify(chatSessionRepository, times(1)).findById(sessionId);
        verify(messageRepository, never()).findByChatSession_IdOrderByCreatedAtAsc(anyLong());
    }
}