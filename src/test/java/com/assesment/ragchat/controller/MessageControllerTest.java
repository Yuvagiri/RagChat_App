package com.assesment.ragchat.controller;

import com.assesment.ragchat.dto.MessageCreateRequest;
import com.assesment.ragchat.entity.ChatSession;
import com.assesment.ragchat.entity.Message;
import com.assesment.ragchat.service.MessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@WebMvcTest(MessageController.class)
class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MessageService messageService;

    @Autowired
    private ObjectMapper objectMapper;

    private final String BASE_URL = "/rag_chat_storage/messages";

    private Message createMockMessage(Long id, Long sessionId, boolean isBot, String content) {
        return Message.builder()
                .id(id)
                .isBot(isBot)
                .content(content)
                .context(isBot ? "context info" : null)
                .createdAt(LocalDateTime.now())
                .chatSession(ChatSession.builder().id(sessionId).build())
                .build();
    }

    // --- POST / ---

    @Test
    void createMessage_validRequest_shouldReturnCreatedMessage() throws Exception {
        Long sessionId = 1L;
        MessageCreateRequest request = new MessageCreateRequest();
        request.setSessionId(sessionId);
        request.setIsBot(false);
        request.setContent("User message content");
        request.setContext(null);

        Message savedMessage = createMockMessage(10L, sessionId, false, request.getContent());

        when(messageService.createMessage(
                eq(sessionId), eq(false), eq(request.getContent()), isNull()))
                .thenReturn(savedMessage);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(10)))
                .andExpect(jsonPath("$.content", is("User message content")));

        verify(messageService, times(1)).createMessage(
                eq(sessionId), eq(false), eq(request.getContent()), isNull());
    }

    @Test
    void createMessage_invalidSessionId_shouldReturnBadRequest() throws Exception {
        Long sessionId = 99L;
        MessageCreateRequest request = new MessageCreateRequest();
        request.setSessionId(sessionId);
        request.setIsBot(true);
        request.setContent("Bot response");

        // Mock the service to throw an exception for invalid session
        when(messageService.createMessage(
                anyLong(), anyBoolean(), anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("Invalid session ID: " + sessionId));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError()); // GlobalExceptionHandler handles this as 500
    }

    // --- GET /{sessionId} ---

    @Test
    void getMessagesBySession_existingSession_shouldReturnMessages() throws Exception {
        Long sessionId = 1L;
        Message m1 = createMockMessage(10L, sessionId, false, "Hi");
        Message m2 = createMockMessage(11L, sessionId, true, "Hello");
        List<Message> messages = List.of(m1, m2);

        when(messageService.getMessagesBySession(sessionId)).thenReturn(messages);

        mockMvc.perform(get(BASE_URL + "/{sessionId}", sessionId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].content", is("Hi")));

        verify(messageService, times(1)).getMessagesBySession(sessionId);
    }

    @Test
    void getMessagesBySession_invalidSession_shouldReturnInternalServerError() throws Exception {
        Long sessionId = 99L;

        when(messageService.getMessagesBySession(sessionId))
                .thenThrow(new IllegalArgumentException("Invalid session ID: " + sessionId));

        mockMvc.perform(get(BASE_URL + "/{sessionId}", sessionId))
                .andExpect(status().isInternalServerError()); // GlobalExceptionHandler handles this as 500
    }
}