package com.xertica.controller;

import com.xertica.dto.UserViewDTO;
import com.xertica.dto.ChatMessageDTO;
import com.xertica.entity.enums.UserRole;
import com.xertica.service.ChatService;
import com.xertica.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/nutritionist")
@RequiredArgsConstructor
@PreAuthorize("hasRole('NUTRITIONIST')") 
public class NutritionistController {

    private final UserService userService;
    private final ChatService chatService;

    @GetMapping("/clients")
    public ResponseEntity<List<UserViewDTO>> getAllClients() {
        List<UserViewDTO> clients = userService.getAllUsers().stream()
                .filter(user -> user.getRole() == UserRole.CLIENT)
                .collect(Collectors.toList());
        return ResponseEntity.ok(clients);
    }

    @GetMapping("/chat/{userId}")
    public ResponseEntity<List<ChatMessageDTO>> getClientChatHistory(@PathVariable Long userId) {
        return ResponseEntity.ok(chatService.getChatHistoryByUserId(userId));
    }

    @PostMapping("/comment/{messageId}")
    public ResponseEntity<ChatMessageDTO> addComment(
            @PathVariable Long messageId,
            @RequestBody Map<String, String> payload,
            Authentication authentication
    ) {
        String comment = payload.get("comment");
        String nutritionistEmail = authentication.getName();

        ChatMessageDTO updatedMessage = chatService.addOrUpdateComment(messageId, comment, nutritionistEmail);
        return ResponseEntity.ok(updatedMessage);
    }
}