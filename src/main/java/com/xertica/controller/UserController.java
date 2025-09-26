package com.xertica.controller;

import com.xertica.dto.*;
import com.xertica.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> approveUser(@PathVariable Long id) {
        userService.approveUser(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<UserViewDTO>> getAllUsers() {
        List<UserViewDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PostMapping("/signup")
    public ResponseEntity<UserViewDTO> signup(@RequestBody UserDTO dto) {
        System.out.println("Recebendo requisição de signup para: " + dto.getEmail());
        UserViewDTO response = userService.signup(dto);
        System.out.println("Signup concluído para: " + dto.getEmail());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody UserLoginDTO dto) {
        System.out.println("Recebendo requisição de login para: " + dto.getEmail());
        LoginResponseDTO response = userService.login(dto);
        return ResponseEntity.ok(response);
    }

    // Criar usuário como ADMIN (já aprovado)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserViewDTO> createUser(@RequestBody UserDTO dto) {
        System.out.println("Admin criando usuário: " + dto.getEmail());
        UserViewDTO response = userService.createUserAsAdmin(dto);
        return ResponseEntity.ok(response);
    }
}