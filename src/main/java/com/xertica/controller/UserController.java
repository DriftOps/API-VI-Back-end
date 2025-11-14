package com.xertica.controller;

import com.xertica.dto.*;
import com.xertica.dto.context.AIContextDTO;
import com.xertica.service.UserService;
import com.xertica.service.UserAnamnesisService;
import com.xertica.entity.User;
import com.xertica.entity.UserAnamnesis;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication; // ✅ CORRETO
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserAnamnesisService anamnesisService;

    // --------------------------------------------------------------------
    // 🔥 ENDPOINTS DE TESTE
    // --------------------------------------------------------------------

    @GetMapping("/test-auth")
    public ResponseEntity<String> testAuth(Authentication auth) {
        if (auth != null && auth.isAuthenticated()) {
            return ResponseEntity.ok(
                    "Autenticado como: " + auth.getName() +
                    " | Roles: " + auth.getAuthorities()
            );
        }
        return ResponseEntity.ok("Não autenticado");
    }

    @GetMapping("/test-admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> testAdmin() {
        return ResponseEntity.ok("Acesso ADMIN permitido!");
    }

    // --------------------------------------------------------------------
    // 🔥 ADMIN: Aprovar Usuário
    // --------------------------------------------------------------------

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> approveUser(@PathVariable Long id) {
        userService.approveUser(id);
        return ResponseEntity.ok().build();
    }

    // --------------------------------------------------------------------
    // 🔥 CRUD USER
    // --------------------------------------------------------------------

    @GetMapping
    public ResponseEntity<List<UserViewDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PostMapping("/signup")
    public ResponseEntity<UserViewDTO> signup(@RequestBody UserDTO dto) {
        return ResponseEntity.ok(userService.signup(dto));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody UserLoginDTO dto) {
        return ResponseEntity.ok(userService.login(dto));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserViewDTO> createUser(@RequestBody UserDTO dto) {
        return ResponseEntity.ok(userService.createUserAsAdmin(dto));
    }

    // --------------------------------------------------------------------
    // 🔥 PERFIL DO USUÁRIO
    // --------------------------------------------------------------------

    @GetMapping("/me")
    public ResponseEntity<UserProfileDTO> getCurrentUser(Authentication authentication) {
        return ResponseEntity.ok(userService.getUserProfileByEmail(authentication.getName()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userService.isSameUser(#id, authentication.name)")
    public ResponseEntity<UserProfileDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserProfile(id));
    }

    // --------------------------------------------------------------------
    // 🔥 ATUALIZAR PERFIL (incluindo endereço / CEP)
    // --------------------------------------------------------------------

    @PutMapping("/profile")
    public ResponseEntity<UserProfileDTO> updateProfile(
            @RequestBody UserUpdateDTO dto,
            Authentication authentication
    ) {
        return ResponseEntity.ok(userService.updateUserProfile(authentication.getName(), dto));
    }

    // --------------------------------------------------------------------
    // 🔥 CONTEXTO PARA A IA
    // --------------------------------------------------------------------

    @GetMapping("/context")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AIContextDTO> getCurrentUserContext(Authentication authentication) {
        return ResponseEntity.ok(userService.getUserContextForAI(authentication.getName()));
    }

    // --------------------------------------------------------------------
    // 🔥 PESAGEM DO USUÁRIO
    // --------------------------------------------------------------------

    @PostMapping("/me/weight")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> addWeightLog(
            Authentication authentication,
            @RequestBody NewWeightLogDTO dto
    ) {
        if (dto.weight() == null || dto.weight() <= 0) {
            return ResponseEntity.badRequest().build();
        }

        userService.addWeightLog(authentication.getName(), dto.weight());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me/weight-history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<WeightLogDTO>> getWeightHistory(Authentication authentication) {
        return ResponseEntity.ok(userService.getWeightHistory(authentication.getName()));
    }
}
