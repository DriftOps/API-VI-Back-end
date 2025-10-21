package com.xertica.repository;

import com.xertica.entity.ChatSession;
import org.springframework.data.jpa.repository.*;

import java.util.List;
import java.util.Optional;

public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {
    @Query("SELECT s FROM ChatSession s WHERE s.user.id = :userId AND s.endedAt IS NULL ORDER BY s.startedAt DESC LIMIT 1")
    Optional<ChatSession> findActiveByUserId(Long userId);
    List<ChatSession> findByUserId(Long userId);
}