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

    // Endpoint para editar meta futura (match com TODO: /api/diets/daily/{id})
    @PutMapping("/daily/{targetId}")
    public ResponseEntity<?> updateDailyTarget(@PathVariable Long targetId, @RequestBody Map<String, Integer> payload) {
        try {
            Integer newCalories = payload.get("newCalories");
            // Fallback se o frontend mandar "adjustedCalories" ou outro nome
            if (newCalories == null) newCalories = payload.get("adjustedCalories");
            
            if (newCalories == null) {
                return ResponseEntity.badRequest().body("Campo 'newCalories' é obrigatório");
            }

            var updatedTarget = dietService.updateDailyTarget(targetId, newCalories);
            return ResponseEntity.ok(updatedTarget);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao atualizar meta: " + e.getMessage());
        }
    }
}