package com.assesment.ragchat.service;

import com.assesment.ragchat.entity.ChatSession;
import com.assesment.ragchat.exception.ResourceNotFoundException;
import com.assesment.ragchat.repo.ChatSessionRepository;
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
class ChatSessionServiceTest {

    @Mock
    private ChatSessionRepository repo;

    @InjectMocks
    private ChatSessionService sessionService;

    private ChatSession createMockSession(Long id, String name, String userName, boolean isFavorite) {
        return ChatSession.builder()
                .id(id != null ? id : 0L)
                .name(name)
                .userName(userName)
                .isFavourite(isFavorite)
                .build();
    }

    @Test
    void createSession_shouldSaveAndReturnSession() {
        String name = "Test Session";
        String userName = "user123";
        ChatSession unsaved = createMockSession(null, name, userName, false);
        ChatSession saved = createMockSession(1L, name, userName, false);

        when(repo.save(any(ChatSession.class))).thenReturn(saved);

        ChatSession result = sessionService.createSession(name, userName);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo(name);
        verify(repo, times(1)).save(any(ChatSession.class));
    }

    @Test
    void getAllSessions_shouldReturnAllSessions() {
        List<ChatSession> sessions = List.of(
                createMockSession(1L, "S1", "u1", false),
                createMockSession(2L, "S2", "u2", false)
        );

        when(repo.findAll()).thenReturn(sessions);

        List<ChatSession> result = sessionService.getAllSessions();

        assertThat(result).hasSize(2);
        verify(repo, times(1)).findAll();
    }

    @Test
    void getSessionsByUserName_shouldReturnFilteredSessions() {
        String userName = "u1";
        List<ChatSession> sessions = List.of(
                createMockSession(1L, "S1", userName, false),
                createMockSession(3L, "S3", userName, true)
        );

        when(repo.findByUserName(userName)).thenReturn(sessions);

        List<ChatSession> result = sessionService.getSessionsByUserName(userName);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getUserName()).isEqualTo(userName);
        verify(repo, times(1)).findByUserName(userName);
    }

    @Test
    void renameSession_existingId_shouldUpdateName() {
        Long id = 1L;
        String oldName = "Old Name";
        String newName = "New Name";
        ChatSession session = createMockSession(id, oldName, "userA", false);

        when(repo.findById(id)).thenReturn(Optional.of(session));
        when(repo.save(any(ChatSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChatSession result = sessionService.renameSession(id, newName);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getName()).isEqualTo(newName);
        verify(repo, times(1)).findById(id);
        verify(repo, times(1)).save(session);
    }

    @Test
    void renameSession_notFound_shouldThrowException() {
        Long id = 99L;
        String newName = "New Name";

        when(repo.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                sessionService.renameSession(id, newName)
        );
        verify(repo, times(1)).findById(id);
        verify(repo, never()).save(any(ChatSession.class));
    }

    @Test
    void deleteSession_existingId_shouldCallDelete() {
        Long id = 1L;
        when(repo.existsById(id)).thenReturn(true);
        doNothing().when(repo).deleteById(id);

        assertDoesNotThrow(() -> sessionService.deleteSession(id));

        verify(repo, times(1)).existsById(id);
        verify(repo, times(1)).deleteById(id);
    }

    @Test
    void deleteSession_notFound_shouldThrowException() {
        Long id = 99L;
        when(repo.existsById(id)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () ->
                sessionService.deleteSession(id)
        );

        verify(repo, times(1)).existsById(id);
        verify(repo, never()).deleteById(id);
    }

    @Test
    void markSessionAsFavorite_shouldUpdateFavoriteStatus() {
        Long id = 1L;
        ChatSession session = createMockSession(id, "Test", "userA", false);

        when(repo.findById(id)).thenReturn(Optional.of(session));
        when(repo.save(any(ChatSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChatSession result = sessionService.markSessionAsFavorite(id, true);

        assertThat(result.isFavourite()).isTrue();

        result = sessionService.markSessionAsFavorite(id, false);

        assertThat(result.isFavourite()).isFalse();

        verify(repo, times(2)).findById(id);
        verify(repo, times(2)).save(session);
    }
}