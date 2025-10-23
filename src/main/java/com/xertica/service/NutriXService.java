package com.xertica.service;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NutriXService {
    
    private final RestTemplate restTemplate;
    
    public NutriXService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }
    
    public String sendMessage(String message, List<Map<String, String>> history) {
        // CORREÇÃO: Use o formato que o FastAPI espera
        Map<String, Object> request = new HashMap<>();
        request.put("pergunta", message); // ← Mude de "message" para "pergunta"
        // Não envie o history se o FastAPI não usa
        
        System.out.println("Enviando para Python: " + request);
        
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                "http://localhost:8001/chat", 
                request, 
                Map.class
            );
            
            Map<String, Object> responseBody = response.getBody();
            System.out.println("Resposta do Python: " + responseBody);
            
            return (String) responseBody.get("resposta");
            
        } catch (Exception e) {
            System.out.println("❌ Erro ao chamar serviço Python: " + e.getMessage());
            // Fallback para não quebrar a aplicação
            return "Desculpe, estou tendo problemas técnicos. Por favor, tente novamente mais tarde.";
        }
    }
}