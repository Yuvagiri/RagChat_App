package com.assesment.ragchat.controller;

import com.assesment.ragchat.dto.ChatSessionCreateRequest;
import com.assesment.ragchat.dto.ChatSessionRenameRequest;
import com.assesment.ragchat.entity.ChatSession;
import com.assesment.ragchat.exception.ResourceNotFoundException;
import com.assesment.ragchat.service.ChatSessionService;
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

@WebMvcTest(ChatSessionController.class)
class ChatSessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatSessionService sessionService;

    @Autowired
    private ObjectMapper objectMapper;

    private final String BASE_URL = "/rag_chat_storage/sessions";

    private ChatSession createMockSession(Long id, String name, String userName) {
        return ChatSession.builder()
                .id(id)
                .name(name)
                .userName(userName)
                .isFavourite(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // --- GET /getAll ---

    @Test
    void retrieveAllSessions_noUserName_shouldReturnAllSessions() throws Exception {
        ChatSession s1 = createMockSession(1L, "Session One", "userA");
        ChatSession s2 = createMockSession(2L, "Session Two", "userB");
        List<ChatSession> allSessions = List.of(s1, s2);

        when(sessionService.getAllSessions()).thenReturn(allSessions);

        mockMvc.perform(get(BASE_URL + "/getAll"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Session One")));

        verify(sessionService, times(1)).getAllSessions();
        verify(sessionService, never()).getSessionsByUserName(anyString());
    }

    @Test
    void retrieveAllSessions_withUserName_shouldReturnFilteredSessions() throws Exception {
        ChatSession s1 = createMockSession(1L, "User A Session", "userA");
        List<ChatSession> userSessions = List.of(s1);
        String userName = "userA";

        when(sessionService.getSessionsByUserName(userName)).thenReturn(userSessions);

        mockMvc.perform(get(BASE_URL + "/getAll").param("userName", userName))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].userName", is(userName)));

        verify(sessionService, times(1)).getSessionsByUserName(userName);
        verify(sessionService, never()).getAllSessions();
    }

    // --- POST /create ---

    @Test
    void createSession_validRequest_shouldReturnCreatedSession() throws Exception {
        ChatSessionCreateRequest request = new ChatSessionCreateRequest();
        request.setName("New Chat");
        request.setUserName("testUser");
        ChatSession createdSession = createMockSession(3L, request.getName(), request.getUserName());

        when(sessionService.createSession(request.getName(), request.getUserName())).thenReturn(createdSession);

        mockMvc.perform(post(BASE_URL + "/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(3)))
                .andExpect(jsonPath("$.name", is("New Chat")))
                .andExpect(jsonPath("$.userName", is("testUser")));

        verify(sessionService, times(1)).createSession(request.getName(), request.getUserName());
    }

    @Test
    void createSession_missingName_shouldReturnBadRequest() throws Exception {
        ChatSessionCreateRequest request = new ChatSessionCreateRequest();
        request.setUserName("testUser"); // Name is missing

        mockMvc.perform(post(BASE_URL + "/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name", is("Session name is required")));
    }

    // --- PATCH /{id}/rename ---

    @Test
    void renameSession_validRequest_shouldReturnRenamedSession() throws Exception {
        Long sessionId = 1L;
        String newName = "Renamed Session";
        ChatSessionRenameRequest request = new ChatSessionRenameRequest();
        request.setName(newName);
        ChatSession renamedSession = createMockSession(sessionId, newName, "testUser");

        when(sessionService.renameSession(sessionId, newName)).thenReturn(renamedSession);

        mockMvc.perform(patch(BASE_URL + "/{id}/rename", sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is(newName)));

        verify(sessionService, times(1)).renameSession(sessionId, newName);
    }

    @Test
    void renameSession_notFound_shouldReturnNotFound() throws Exception {
        Long sessionId = 99L;
        String newName = "Renamed Session";
        ChatSessionRenameRequest request = new ChatSessionRenameRequest();
        request.setName(newName);

        when(sessionService.renameSession(sessionId, newName)).thenThrow(
                new ResourceNotFoundException("Chat Session not found with ID: " + sessionId));

        mockMvc.perform(patch(BASE_URL + "/{id}/rename", sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Resource Not Found")));
    }

    // --- PATCH /{id}/favorite ---

    @Test
    void markSessionAsFavorite_shouldReturnFavoriteSession() throws Exception {
        Long sessionId = 1L;
        ChatSession favoritedSession = createMockSession(sessionId, "Test", "userA");
        favoritedSession.setFavourite(true);
        Boolean isFavorite = true;

        when(sessionService.markSessionAsFavorite(sessionId, isFavorite)).thenReturn(favoritedSession);

        mockMvc.perform(patch(BASE_URL + "/{id}/favorite", sessionId)
                        .param("isFavorite", isFavorite.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.favourite", is(true)));

        verify(sessionService, times(1)).markSessionAsFavorite(sessionId, isFavorite);
    }

    // --- DELETE /delete/{id} ---

    @Test
    void deleteSession_existingId_shouldReturnNoContent() throws Exception {
        Long sessionId = 1L;
        doNothing().when(sessionService).deleteSession(sessionId);

        mockMvc.perform(delete(BASE_URL + "/delete/{id}", sessionId))
                .andExpect(status().isNoContent());

        verify(sessionService, times(1)).deleteSession(sessionId);
    }

    @Test
    void deleteSession_notFound_shouldReturnNotFound() throws Exception {
        Long sessionId = 99L;
        doThrow(new ResourceNotFoundException("ChatSession", "id", sessionId))
                .when(sessionService).deleteSession(sessionId);

        mockMvc.perform(delete(BASE_URL + "/delete/{id}", sessionId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Resource Not Found")));
    }
}