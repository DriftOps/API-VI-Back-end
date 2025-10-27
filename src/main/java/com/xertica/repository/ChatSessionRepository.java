package com.xertica.repository;

import com.xertica.entity.ChatSession;
import com.xertica.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {
    Optional<ChatSession> findByUserAndEndedAtIsNull(User user);
    Optional<ChatSession> findByUserIdAndEndedAtIsNull(Long userId);
}