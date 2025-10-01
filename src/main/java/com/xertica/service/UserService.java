package com.xertica.service;

import com.xertica.dto.*;
import com.xertica.entity.*;
import com.xertica.entity.enums.GoalType;
import com.xertica.entity.enums.ActivityLevelType; // ✅ IMPORTAR ActivityLevelType
import com.xertica.entity.enums.UserRole;
import com.xertica.repository.*;
import com.xertica.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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

    // Listar todos usuários
    public List<UserViewDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toUserViewDTO)
                .collect(Collectors.toList());
    }

    // Signup
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

    // Login
    public LoginResponseDTO login(UserLoginDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuário ou senha inválidos"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new RuntimeException("Usuário ou senha inválidos");
        }

        // Para ADMIN, não exigir aprovação
        if (!user.getApproved() && user.getRole() != UserRole.ADMIN) {
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

    @Transactional
    public void approveUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        user.setApproved(true);
        userRepository.save(user);

        // Envia e-mail (com try-catch para não quebrar a aprovação)
        try {
            String subject = "Sua conta foi aprovada!";
            String text = "Olá " + user.getName() + ",\n\n" +
                    "Sua conta no NutriX foi aprovada pelo administrador. Agora você pode acessar o sistema normalmente.\n\n"
                    +
                    "Atenciosamente,\nEquipe NutriX";

            emailService.sendEmail(user.getEmail(), subject, text);
            System.out.println("Email enviado para: " + user.getEmail());
        } catch (Exception e) {
            System.out.println("❌ Erro ao enviar email, mas usuário foi aprovado: " + e.getMessage());
            // Não relança a exceção - a aprovação foi bem sucedida
        }
    }

    // Buscar usuário por email (apenas dados básicos)
    public UserViewDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        return toUserViewDTO(user);
    }

    // Buscar perfil completo do usuário por email
    public UserProfileDTO getUserProfileByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        return toUserProfileDTO(user);
    }

    // Buscar perfil completo do usuário por ID
    public UserProfileDTO getUserProfile(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        return toUserProfileDTO(user);
    }

    // Atualizar perfil do usuário (chaves fechadas corretamente)
    @Transactional
    public UserProfileDTO updateUserProfile(String email, UserUpdateDTO dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // Atualizar campos básicos (convertendo String para enum quando necessário)
        if (dto.getGoal() != null) {
            try {
                user.setGoal(GoalType.valueOf(dto.getGoal())); // Converter String para Enum
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Valor de objetivo inválido: " + dto.getGoal());
            }
        } // ✅ FECHAR CHAVE DO IF DO GOAL

        if (dto.getWeight() != null) user.setWeight(dto.getWeight());
        if (dto.getHeight() != null) user.setHeight(dto.getHeight());

        // Converter String para LocalDate
        if (dto.getBirthDate() != null) {
            user.setBirthDate(LocalDate.parse(dto.getBirthDate()));
        }

        // Converter String para Enum
        if (dto.getActivityLevel() != null) {
            try {
                user.setActivityLevel(ActivityLevelType.valueOf(dto.getActivityLevel()));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Valor de nível de atividade inválido: " + dto.getActivityLevel());
            }
        } // ✅ FECHAR CHAVE DO IF DO ACTIVITY_LEVEL

        if (dto.getPlan() != null) user.setPlan(dto.getPlan());

        // Atualizar preferências alimentares
        if (dto.getDietaryPreferences() != null) {
            // Remove preferências existentes
            userPreferenceRepository.deleteByUser(user);

            // Adiciona novas preferências
            for (String prefName : dto.getDietaryPreferences()) {
                DietaryPreference pref = preferenceRepository.findByName(prefName)
                        .orElseGet(() -> preferenceRepository.save(new DietaryPreference(null, prefName)));
                userPreferenceRepository.save(new UserPreference(user, pref));
            }
        }

        // Atualizar restrições alimentares
        if (dto.getRestrictions() != null) {
            // Remove restrições existentes
            userRestrictionRepository.deleteByUser(user);

            // Adiciona novas restrições
            for (String resName : dto.getRestrictions()) {
                DietaryRestriction res = restrictionRepository.findByName(resName)
                        .orElseGet(() -> restrictionRepository.save(new DietaryRestriction(null, resName)));
                userRestrictionRepository.save(new UserRestriction(user, res));
            }
        }

        userRepository.save(user);
        return toUserProfileDTO(user);
    }

    // Método para verificar se é o mesmo usuário (usado no @PreAuthorize)
    public boolean isSameUser(Long userId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        return user.getId().equals(userId);
    }

    // ===== HELPERS =====

    // Converter User para UserViewDTO (dados básicos)
    private UserViewDTO toUserViewDTO(User user) {
    return new UserViewDTO(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getRole(),
            user.getApproved()
    );
    }

    // Converter User para UserProfileDTO (dados completos)
    private UserProfileDTO toUserProfileDTO(User user) {
        // Buscar preferências do usuário
        List<String> preferences = userPreferenceRepository.findByUser(user)
                .stream()
                .map(up -> up.getPreference().getName())
                .collect(Collectors.toList());

        // Buscar restrições do usuário
        List<String> restrictions = userRestrictionRepository.findByUser(user)
                .stream()
                .map(ur -> ur.getRestriction().getName())
                .collect(Collectors.toList());

        // Converter LocalDate para String
        String birthDateStr = user.getBirthDate() != null ? user.getBirthDate().toString() : null;

        // ✅ CONVERTER ENUMS PARA STRING
        String goalStr = user.getGoal() != null ? user.getGoal().name() : null;
        String activityLevelStr = user.getActivityLevel() != null ? user.getActivityLevel().name() : null;
        String roleStr = user.getRole() != null ? user.getRole().name() : null;

        return new UserProfileDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                roleStr,
                goalStr,
                user.getWeight(),
                user.getHeight(),
                birthDateStr,
                activityLevelStr,
                preferences,
                restrictions,
                user.getPlan(),
                user.getApproved()
        );
    }
}