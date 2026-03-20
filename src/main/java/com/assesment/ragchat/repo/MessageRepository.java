package com.assesment.ragchat.repo;

import com.assesment.ragchat.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByChatSession_IdOrderByCreatedAtAsc(Long sessionId);
}
