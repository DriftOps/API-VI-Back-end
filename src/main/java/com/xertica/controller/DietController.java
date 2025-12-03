package com.xertica.controller;

import com.xertica.dto.CreateDietRequestDTO;
import com.xertica.dto.DietViewDTO;
import com.xertica.service.DietService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/diets")
public class DietController {

    private final DietService dietService;

    public DietController(DietService dietService) {
        this.dietService = dietService;
    }

    @PostMapping
    public ResponseEntity<?> createDiet(@Valid @RequestBody CreateDietRequestDTO request) {
        try {
            DietViewDTO dto = dietService.createDiet(request);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Erro ao criar dieta: " + e.getMessage());
        }
    } 

    @GetMapping("/active/{userId}")
    public ResponseEntity<DietViewDTO> getActiveDiet(@PathVariable Long userId) {
        try {
            DietViewDTO dietDTO = dietService.getActiveDietForUser(userId);
            return ResponseEntity.ok(dietDTO);
        } catch (Exception e) {
            return ResponseEntity.notFound().build(); // Retorna 404 se não achar dieta, útil para o frontend saber
        }
    }

    // --- NOVOS ENDPOINTS ---

    // Endpoint para cancelar dieta (match com URL: /api/diets/cancel/{id})
    @PostMapping("/cancel/{id}")
    public ResponseEntity<Void> cancelDiet(@PathVariable Long id) {
        try {
            dietService.cancelDiet(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/daily/{targetId}")
public ResponseEntity<?> updateDailyTarget(@PathVariable Long targetId, @RequestBody Map<String, Object> payload) { // Mudei para Object
    try {
        // Extrai calorias (seguro contra nulos)
        Integer newCalories = null;
        if (payload.get("newCalories") != null) {
            newCalories = Integer.parseInt(payload.get("newCalories").toString());
        } else if (payload.get("adjustedCalories") != null) {
            newCalories = Integer.parseInt(payload.get("adjustedCalories").toString());
        }

        // Extrai menu sugerido
        String suggestedMenu = (String) payload.get("suggestedMenu");
        
        // Validação: Pelo menos um deve ser enviado
        if (newCalories == null && suggestedMenu == null) {
            return ResponseEntity.badRequest().body("Deve enviar 'newCalories' ou 'suggestedMenu'");
        }

        // Chama o serviço atualizado
        var updatedTarget = dietService.updateDailyTarget(targetId, newCalories, suggestedMenu);
        return ResponseEntity.ok(updatedTarget);
    } catch (Exception e) {
        return ResponseEntity.badRequest().body("Erro ao atualizar meta: " + e.getMessage());
        }
    }
}