package com.xertica.service;

import com.xertica.dto.*;
import com.xertica.entity.*;
import com.xertica.entity.enums.UserRole;
import com.xertica.repository.*;
import com.xertica.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final DietaryPreferenceRepository preferenceRepository;
    private final DietaryRestrictionRepository restrictionRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final UserRestrictionRepository userRestrictionRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    // Criar usuário (admin/geral)
    @Transactional
    public UserViewDTO signup(UserDTO dto) {
        if (dto.getRole() == null) {
            dto.setRole(UserRole.CLIENT);
        }

        // Garanta que chatHistory seja um JSON válido
        String chatHistory = dto.getChatHistory();
        if (chatHistory == null || chatHistory.trim().isEmpty()) {
            chatHistory = "[]"; // JSON array vazio
        }

        User user = User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(dto.getRole())
                .goal(dto.getGoal())
                .height(dto.getHeight())
                .weight(dto.getWeight())
                .birthDate(dto.getBirthDate())
                .activityLevel(dto.getActivityLevel())
                .chatHistory(chatHistory)
                .plan(dto.getPlan())
                .approved(false)
                .build();

        userRepository.save(user);
        return toUserViewDTO(user);
    }

    // Listar todos usuários
    public List<UserViewDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toUserViewDTO)
                .collect(Collectors.toList());
    }

    // Login
    public LoginResponseDTO login(UserLoginDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuário ou senha inválidos"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new RuntimeException("Usuário ou senha inválidos");
        }
        if (!user.getApproved()) {
            throw new RuntimeException("Usuário ainda não aprovado pelo administrador.");
        }

        String token = JwtUtils.generateToken(user.getEmail(), user.getId());

        return new LoginResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                token);
    }

    // ===== Helpers =====
    private UserViewDTO toUserViewDTO(User user) {
        return new UserViewDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole());
    }

    // private UserDTO dtoToUserDTO(UserCreateDTO dto) {
    // return new UserDTO(
    // dto.getName(),
    // dto.getEmail(),
    // dto.getPassword(),
    // dto.getRole(),
    // dto.getGoal(),
    // dto.getHeight(),
    // dto.getWeight(),
    // dto.getBirthDate(),
    // dto.getActivityLevel(),
    // dto.getPreferences(),
    // dto.getRestrictions(),
    // null,
    // null);
    // }

    @Transactional
    public void approveUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        user.setApproved(true);
        userRepository.save(user);

        // Envia e-mail
        String subject = "Sua conta foi aprovada!";
        String text = "Olá " + user.getName() + ",\n\n" +
                "Sua conta no NutriX foi aprovada pelo administrador. Agora você pode acessar o sistema normalmente.\n\n"
                +
                "Atenciosamente,\nEquipe NutriX";

        emailService.sendEmail(user.getEmail(), subject, text);
    }

    //Criar usuário como ADMIN (já aprovado)
    @Transactional
    public UserViewDTO createUserAsAdmin(UserDTO dto) {
        if (dto.getRole() == null) {
            dto.setRole(UserRole.CLIENT);
        }

        // Garanta que chatHistory seja um JSON válido
        String chatHistory = dto.getChatHistory();
        if (chatHistory == null || chatHistory.trim().isEmpty()) {
            chatHistory = "[]";
        }

        User user = User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(dto.getRole())
                .goal(dto.getGoal())
                .height(dto.getHeight())
                .weight(dto.getWeight())
                .birthDate(dto.getBirthDate())
                .activityLevel(dto.getActivityLevel())
                .chatHistory(chatHistory)
                .plan(dto.getPlan())
                .approved(true) // 🔥 DIFERENÇA: Já cria aprovado
                .build();

        userRepository.save(user);
        return toUserViewDTO(user);
    }
}
