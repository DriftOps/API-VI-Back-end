package com.xertica.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NutriXService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String AI_URL = "http://localhost:8001/chat"; // FastAPI

    public String sendMessage(String message, List<Map<String, String>> history) {
        Map<String, Object> request = new HashMap<>();
        request.put("message", message);
        request.put("history", history);

        ResponseEntity<Map> response = restTemplate.postForEntity(AI_URL, request, Map.class);
        return (String) response.getBody().get("reply");
    }
}
