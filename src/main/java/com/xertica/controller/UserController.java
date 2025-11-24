package com.xertica.controller;

import com.xertica.dto.*;
import com.xertica.dto.context.AIContextDTO;
import com.xertica.service.UserService;
import com.xertica.service.UserAnamnesisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserAnamnesisService anamnesisService;

    // 🔥 ENDPOINT DE TESTE
    @GetMapping("/test-auth")
    public ResponseEntity<String> testAuth() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return ResponseEntity.ok("Autenticado como: " + authentication.getName() +
                    " | Roles: " + authentication.getAuthorities());
        } else {
            return ResponseEntity.ok("Não autenticado");
        }
    }

    // 🔥 ENDPOINT DE TESTE ADMIN
    @GetMapping("/test-admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> testAdmin() {
        return ResponseEntity.ok("Acesso ADMIN permitido!");
    }

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

    // Buscar usuário atual
    @GetMapping("/me")
    public ResponseEntity<UserProfileDTO> getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        UserProfileDTO user = userService.getUserProfileByEmail(email);
        return ResponseEntity.ok(user);
    }

    // Buscar usuário por ID (apenas ADMIN ou o próprio usuário)
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userService.isSameUser(#id, authentication.name)")
    public ResponseEntity<UserProfileDTO> getUserById(@PathVariable Long id) {
        UserProfileDTO user = userService.getUserProfile(id);
        return ResponseEntity.ok(user);
    }

    // Atualizar perfil do usuário
    @PutMapping("/profile")
    public ResponseEntity<UserProfileDTO> updateProfile(@RequestBody UserUpdateDTO dto, Authentication authentication) {
        String email = authentication.getName();
        UserProfileDTO updatedUser = userService.updateUserProfile(email, dto);
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping("/context")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AIContextDTO> getCurrentUserContext(Authentication authentication) {
        String email = authentication.getName();
        AIContextDTO context = userService.getUserContextForAI(email);
        return ResponseEntity.ok(context);
    }

    @PostMapping("/me/weight")
@PreAuthorize("isAuthenticated()")
public ResponseEntity<Void> addWeightLog(
        Authentication authentication, 
        @RequestBody NewWeightLogDTO dto) {
            
    if (dto.weight() == null || dto.weight() <= 0) {
        return ResponseEntity.badRequest().build();
    }
    
    String email = authentication.getName();
    userService.addWeightLog(email, dto.weight());
    return ResponseEntity.ok().build();
}

@GetMapping("/me/weight-history")
@PreAuthorize("isAuthenticated()")
public ResponseEntity<List<WeightLogDTO>> getWeightHistory(Authentication authentication) {
    String email = authentication.getName();
    List<WeightLogDTO> history = userService.getWeightHistory(email);
    return ResponseEntity.ok(history);
}
}