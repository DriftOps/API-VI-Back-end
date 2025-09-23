package com.xertica.controller;

import com.xertica.dto.*;
import com.xertica.entity.User;
import com.xertica.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // Criar usuário direto (admin/geral)
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody UserDTO dto) {
        User user = userService.createUser(dto);
        return ResponseEntity.ok(user);
    }

    // Listar todos usuários
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    // Signup (usuário se cadastra)
    @PostMapping("/signup")
    public ResponseEntity<UserViewDTO> signup(@RequestBody UserCreateDTO dto) {
        UserViewDTO response = userService.signup(dto);
        return ResponseEntity.ok(response);
    }

    // Login (retorna JWT)
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody UserLoginDTO dto) {
        LoginResponseDTO response = userService.login(dto);
        return ResponseEntity.ok(response);
    }
}