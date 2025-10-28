package com.xertica.controller;

import com.xertica.dto.ChatMessageDTO;
import com.xertica.dto.NewMessageRequest;
import com.xertica.service.ChatService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    // Pega o histórico do usuário LOGADO
    @GetMapping("/history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ChatMessageDTO>> getChatHistory(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(chatService.getChatHistory(email));
    }

    // Envia uma mensagem do usuário LOGADO
    @PostMapping("/send")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ChatMessageDTO> sendMessage(
            Authentication authentication,
            @RequestBody NewMessageRequest request,
            HttpServletRequest httpServletRequest // Para pegar o token original
    ) {
        String email = authentication.getName();
        String token = httpServletRequest.getHeader("Authorization"); // Pega "Bearer ..."
        
        ChatMessageDTO aiResponse = chatService.saveNewMessage(email, request, token);
        return ResponseEntity.ok(aiResponse);
    }

    @PostMapping("/feedback/{messageId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> giveFeedback(
            Authentication authentication,
            @PathVariable Long messageId,
            @RequestBody Map<String, String> payload // Espera {"feedback": "positive"} ou {"feedback": "negative"}
    ) {
        String email = authentication.getName();
        String feedback = payload.get("feedback"); // "positive", "negative", ou null
        
        chatService.saveFeedback(email, messageId, feedback);
        return ResponseEntity.ok().build();
    }
}
    