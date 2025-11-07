package com.xertica.controller;

import com.xertica.dto.CreateDietRequestDTO;
import com.xertica.dto.DietViewDTO;
import com.xertica.service.DietService;
import com.xertica.security.JwtUtils; // Para pegar o usuário do token
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/diets")
public class DietController {

    private final DietService dietService;
    // private final JwtUtils jwtUtils; // Para pegar o user_id do token

    public DietController(DietService dietService) {
        this.dietService = dietService;
    }

    @PostMapping
    public ResponseEntity<DietViewDTO> createDiet(@Valid @RequestBody CreateDietRequestDTO request) {
        // Em um app real, o userId viria do token de autenticação
        // Long userId = jwtUtils.getUserIdFromToken(token);
        // request.setUserId(userId); 
        
        DietViewDTO dto = dietService.createDiet(request);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/active/{userId}") // Trocado para pegar por ID por enquanto
    public ResponseEntity<DietViewDTO> getActiveDiet(@PathVariable Long userId) {
        // Pegar o userId do token de autenticação
        // Long currentUserId = jwtUtils.getUserIdFromToken(token);
        
        DietViewDTO dietDTO = dietService.getActiveDietForUser(userId);
        return ResponseEntity.ok(dietDTO);
    }

    // TODO: Adicionar endpoints para
    // PUT /api/diets/daily/{id} (para editar uma meta futura)
    // POST /api/diets/{id}/cancel (para cancelar uma dieta)
}