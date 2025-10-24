package com.xertica.controller;

import com.xertica.dto.UserGoalDTO;
import com.xertica.service.UserGoalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
public class UserGoalController {

    private final UserGoalService goalService;

    @GetMapping
    public ResponseEntity<List<UserGoalDTO>> getUserGoals(Authentication authentication) {
        String email = authentication.getName();
        List<UserGoalDTO> goals = goalService.getUserGoals(email);
        return ResponseEntity.ok(goals);
    }

    @PostMapping
    public ResponseEntity<UserGoalDTO> createGoal(@RequestBody UserGoalDTO dto, Authentication authentication) {
        String email = authentication.getName();
        UserGoalDTO created = goalService.createGoal(email, dto);
        return ResponseEntity.ok(created);
    }
}
