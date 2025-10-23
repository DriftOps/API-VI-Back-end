package com.xertica.controller;

import com.xertica.entity.*;
import com.xertica.repository.*;
import com.xertica.service.NutriXService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/nutrix")
@RequiredArgsConstructor
public class NutriXController {

    private final NutriXService nutrixClient;
    private final ChatSessionRepository sessionRepo;
    private final ChatMessageRepository messageRepo;
    private final UserRepository userRepo;

    @PostMapping("/chat/{userId}")
    @Transactional
    public ResponseEntity<?> chat(@PathVariable Long userId, @RequestBody Map<String, String> payload) {
        String message = payload.get("message");

        // Busca ou cria sessão ativa
        ChatSession session = sessionRepo.findActiveByUserId(userId)
                .orElseGet(() -> {
                    ChatSession newSession = new ChatSession();
                    userRepo.findById(userId).ifPresent(newSession::setUser);
                    return sessionRepo.save(newSession);
                });

        // 2Monta histórico pro Python
        List<Map<String, String>> formatted = session.getMessages() != null
                ? session.getMessages().stream()
                    .map(m -> Map.of("sender", m.getSender(), "text", m.getMessage()))
                    .toList()
                : List.of();

        // 3️⃣ Envia pro FastAPI (NutriX IA)
        String reply = nutrixClient.sendMessage(message, formatted);

        // 4️⃣ Salva mensagens no banco
        ChatMessage userMsg = new ChatMessage(null, session, "user", message, LocalDateTime.now());
        ChatMessage aiMsg = new ChatMessage(null, session, "assistant", reply, LocalDateTime.now());
        messageRepo.save(userMsg);
        messageRepo.save(aiMsg);

        return ResponseEntity.ok(Map.of("reply", reply));
    }

    @GetMapping("/history/{userId}")
    public ResponseEntity<?> getChatHistory(@PathVariable Long userId) {
        List<ChatSession> sessions = sessionRepo.findByUserId(userId);
        List<Map<String, Object>> history = sessions.stream().map(session -> Map.of(
                "sessionId", session.getId(),
                "startedAt", session.getStartedAt(),
                "messages", session.getMessages().stream().map(m -> Map.of(
                        "sender", m.getSender(),
                        "text", m.getMessage(),
                        "timestamp", m.getTimestamp()
                )).toList()
        )).toList();

        return ResponseEntity.ok(history);
    }
}