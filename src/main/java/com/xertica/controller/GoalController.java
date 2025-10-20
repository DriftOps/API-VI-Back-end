package com.xertica.controller;

import com.xertica.dto.GoalDTO;
import com.xertica.service.GoalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
public class GoalController {

    private final GoalService goalService;

    @PostMapping
    public ResponseEntity<GoalDTO> createGoal(@RequestBody GoalDTO dto) {
        return ResponseEntity.ok(goalService.createGoal(dto));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<GoalDTO>> getGoalsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(goalService.getGoalsByUser(userId));
    }

    @PatchMapping("/{id}/progress")
    public ResponseEntity<GoalDTO> updateProgress(@PathVariable Long id, @RequestParam Double progress) {
        return ResponseEntity.ok(goalService.updateProgress(id, progress));
    }

    @GetMapping("/user/{userId}/average-progress")
    public ResponseEntity<Double> getAverageProgress(@PathVariable Long userId) {
        return ResponseEntity.ok(goalService.calculateAverageProgress(userId));
    }
}
