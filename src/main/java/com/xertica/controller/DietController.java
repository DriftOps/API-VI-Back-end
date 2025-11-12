// src/main/java/com/xertica/controller/DietController.java
package com.xertica.controller;

import com.xertica.dto.CreateDietRequestDTO;
import com.xertica.dto.DietDailyTargetDTO; // (NOVO)
import com.xertica.dto.DietViewDTO;
import com.xertica.dto.UpdateDailyTargetDTO; // (NOVO)
import com.xertica.entity.User;
import com.xertica.service.DietService;
import com.xertica.service.UserService;
import org.springframework.http.ResponseEntity; // (NOVO)
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/diets")
public class DietController {

    private final DietService dietService;
    private final UserService userService;

    public DietController(DietService dietService, UserService userService) {
        this.dietService = dietService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<DietViewDTO> createDiet(@Valid @RequestBody CreateDietRequestDTO request) {
        DietViewDTO dto = dietService.createDiet(request);
        return ResponseEntity.ok(dto);
    }
    
    @GetMapping("/active")
    public ResponseEntity<DietViewDTO> getActiveDiet(@RequestHeader("Authorization") String token) {
        User user = userService.getUserFromToken(token); 
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        
        // (CORRIGIDO) Agora lida com o Optional
        return dietService.getActiveDietForUser(user.getId())
                .map(ResponseEntity::ok) // Se presente, retorna 200 OK com a dieta
                .orElse(ResponseEntity.notFound().build()); // Se vazio, retorna 404 Not Found
    }

    // ==========================================================
    // TODO 1: IMPLEMENTADO
    // ==========================================================
    @PutMapping("/daily/{id}")
    public ResponseEntity<DietDailyTargetDTO> updateDailyTarget(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDailyTargetDTO dto,
            @RequestHeader("Authorization") String token) {
        
        User user = userService.getUserFromToken(token);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        
        DietDailyTargetDTO updatedTarget = dietService.updateDailyTarget(id, dto, user.getId());
        return ResponseEntity.ok(updatedTarget);
    }

    // ==========================================================
    // TODO 2: IMPLEMENTADO
    // ==========================================================
    @PostMapping("/cancel/{id}")
    public ResponseEntity<Void> cancelDiet(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        
        User user = userService.getUserFromToken(token);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        
        dietService.cancelDiet(id, user.getId());
        return ResponseEntity.ok().build();
    }
}