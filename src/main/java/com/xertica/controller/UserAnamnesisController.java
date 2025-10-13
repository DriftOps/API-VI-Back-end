package com.xertica.controller;

import com.xertica.dto.UserAnamnesisDTO;
import com.xertica.entity.UserAnamnesis;
import com.xertica.mapper.UserAnamnesisMapper;
import com.xertica.service.UserAnamnesisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/anamnesis")
@RequiredArgsConstructor
public class UserAnamnesisController {

    private final UserAnamnesisService anamnesisService;

    @GetMapping("/{userId}")
    public ResponseEntity<UserAnamnesisDTO> getByUser(@PathVariable Long userId) {
        UserAnamnesis anamnesis = anamnesisService.getByUserId(userId);
        return ResponseEntity.ok(UserAnamnesisMapper.toDTO(anamnesis));
    }

    @PostMapping("/{userId}")
    public ResponseEntity<UserAnamnesisDTO> create(@PathVariable Long userId, @RequestBody UserAnamnesisDTO dto) {
        UserAnamnesis created = anamnesisService.create(userId, UserAnamnesisMapper.toEntity(dto));
        return ResponseEntity.ok(UserAnamnesisMapper.toDTO(created));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserAnamnesisDTO> update(@PathVariable Long userId, @RequestBody UserAnamnesisDTO dto) {
        UserAnamnesis updated = anamnesisService.update(userId, UserAnamnesisMapper.toEntity(dto));
        return ResponseEntity.ok(UserAnamnesisMapper.toDTO(updated));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> delete(@PathVariable Long userId) {
        anamnesisService.delete(userId);
        return ResponseEntity.noContent().build();
    }
}