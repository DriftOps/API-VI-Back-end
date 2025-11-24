package com.xertica.service;

import com.xertica.dto.ChatMessageDTO; 
import com.xertica.dto.NewMessageRequest;
import com.xertica.entity.ChatMessage;
import com.xertica.entity.ChatSession;
import com.xertica.entity.User;
import com.xertica.entity.enums.UserRole;
import com.xertica.repository.ChatMessageRepository;
import com.xertica.repository.ChatSessionRepository;
import com.xertica.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final UserRepository userRepository;
    private final ChatSessionRepository sessionRepository;
    private final ChatMessageRepository messageRepository;
    private final RestTemplate restTemplate; // Injetado
    private final UserService userService; // Para pegar o token

    @Transactional(readOnly = true)
    public List<ChatMessageDTO> getChatHistory(String email) {
        User user = userService.findUserByEmail(email);
        return sessionRepository.findByUserAndEndedAtIsNull(user)
                .map(session -> messageRepository.findBySessionIdOrderByTimestampAsc(session.getId())
                        .stream()
                        .map(this::toDTO)
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    @Transactional
    public ChatMessageDTO saveNewMessage(String email, NewMessageRequest request, String userToken) {
        User user = userService.findUserByEmail(email);
        ChatSession session = sessionRepository.findByUserAndEndedAtIsNull(user)
                .orElseGet(() -> createNewSession(user));

        // 1. Salva a mensagem do Usuário
        ChatMessage userMessage = ChatMessage.builder()
                .session(session)
                .sender("user")
                .message(request.getMessage())
                .image(request.getImage())
                .timestamp(LocalDateTime.now())
                .build();
        messageRepository.save(userMessage);

        // 2. Chama a AI (Python Backend)
        String aiResponse = callPythonAI(request.getMessage(), userToken, request.getImage());

        // 3. Salva a resposta da AI
        ChatMessage aiMessage = ChatMessage.builder()
                .session(session)
                .sender("assistant")
                .message(aiResponse)
                .timestamp(LocalDateTime.now())
                .build();
        ChatMessage savedAiMessage = messageRepository.save(aiMessage);

        return toDTO(savedAiMessage); // Retorna a resposta da AI
    }
    
    private String callPythonAI(String message, String userToken, String imageBase64) { // Adicione imageBase64 aos parâmetros
        String aiUrl = "http://localhost:8001/responder";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (userToken != null && !userToken.isEmpty()) {
             headers.set("Authorization", userToken);
        }
       
        // Cria um Map mutável ou use HashMap para poder adicionar chaves condicionalmente
        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("pergunta", message);
        
        if (imageBase64 != null && !imageBase64.isEmpty()) {
            body.put("image", imageBase64); // Envia a imagem para o Python
        }

        HttpEntity<java.util.Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            // A resposta do Python é {"resposta": "...", "meal_saved": true/false}
            Map<String, Object> response = restTemplate.postForObject(aiUrl, entity, Map.class);
            
            if (response != null && response.get("meal_saved") == Boolean.TRUE) {
                // TODO: Adicionar lógica para atualizar refeições aqui no backend
                System.out.println("AI salvou uma refeição. Lógica de backend pendente.");
            }
            
            return (String) response.get("resposta");
        } catch (Exception e) {
            System.err.println("Erro ao chamar AI: " + e.getMessage());
            return "Desculpe, não consegui processar sua resposta no momento.";
        }
    }

    private ChatSession createNewSession(User user) {
        ChatSession session = ChatSession.builder()
                .user(user)
                .startedAt(LocalDateTime.now())
                .build();
        return sessionRepository.save(session);
    }

    // Converte Entidade para DTO
    public ChatMessageDTO toDTO(ChatMessage entity) {
        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setId(entity.getId());
        dto.setSessionId(entity.getSession().getId());
        dto.setUserId(entity.getSession().getUser().getId()); // Útil para o admin
        dto.setSender(entity.getSender());
        dto.setMessage(entity.getMessage());
        dto.setImage(entity.getImage());
        dto.setTimestamp(entity.getTimestamp());
        dto.setNutritionistComment(entity.getNutritionistComment());
        dto.setUserFeedback(entity.getUserFeedback());
        return dto;
    }
    
    // --- Métodos para Fase 2 (Nutricionista) ---
    
    @Transactional(readOnly = true)
    public List<ChatMessageDTO> getChatHistoryByUserId(Long userId) {
        return sessionRepository.findByUserIdAndEndedAtIsNull(userId)
                .map(session -> messageRepository.findBySessionIdOrderByTimestampAsc(session.getId())
                        .stream()
                        .map(this::toDTO)
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    @Transactional
    public ChatMessageDTO addOrUpdateComment(Long messageId, String comment, String nutritionistEmail) {
        User nutritionist = userService.findUserByEmail(nutritionistEmail);
        if (nutritionist.getRole() != UserRole.NUTRITIONIST) {
            throw new SecurityException("Usuário não é um nutricionista.");
        }
        
        ChatMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException("Mensagem não encontrada"));
        
        message.setNutritionist(nutritionist);
        message.setNutritionistComment(comment);
        message.setCommentTimestamp(LocalDateTime.now());
        
        return toDTO(messageRepository.save(message));
    }

    @Transactional
    public void saveFeedback(String email, Long messageId, String feedback) {
        User user = userService.findUserByEmail(email);
        
        ChatMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException("Mensagem não encontrada"));

        if (!message.getSession().getUser().getId().equals(user.getId())) {
            throw new SecurityException("Usuário não autorizado a dar feedback nesta mensagem.");
        }
        
        if ("positive".equals(feedback) || "negative".equals(feedback)) {
            message.setUserFeedback(feedback);
        } else {
            message.setUserFeedback(null);
        }

        messageRepository.save(message);
    }
}