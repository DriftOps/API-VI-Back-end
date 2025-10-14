package com.xertica.service;

import com.xertica.dto.*;
import com.xertica.entity.*;
import com.xertica.entity.enums.UserRole;
import com.xertica.entity.enums.anamnesis.MainGoalType;
import com.xertica.repository.*;
import com.xertica.security.JwtUtils;
import jakarta.persistence.EntityNotFoundException;
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
    private final UserAnamnesisRepository anamnesisRepository; // ✅ INJETADO
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    // ✅ MÉTODO ATUALIZADO
    @Transactional
    public UserViewDTO signup(UserDTO dto) {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalStateException("O e-mail informado já está em uso.");
        }

        User user = User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(UserRole.CLIENT) // Papel padrão para signup
                .height(dto.getHeight())
                .weight(dto.getWeight())
                .birthDate(dto.getBirthDate())
                .gender(dto.getGender())
                .approved(false) // Novos usuários precisam de aprovação
                .build();

        userRepository.save(user);
        return toUserViewDTO(user);
    }

    // ✅ MÉTODO ATUALIZADO para usar o DTO simplificado
    @Transactional
    public UserViewDTO createUserAsAdmin(UserDTO dto) {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalStateException("O e-mail informado já está em uso.");
        }

        User user = User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(dto.getRole() != null ? dto.getRole() : UserRole.CLIENT)
                .height(dto.getHeight())
                .weight(dto.getWeight())
                .birthDate(dto.getBirthDate())
                .gender(dto.getGender())
                .approved(true) // Admin cria usuários já aprovados
                .build();

        userRepository.save(user);
        return toUserViewDTO(user);
    }
    
    // ✅ MÉTODO ATUALIZADO para lidar com a atualização do 'goal' na anamnese
    @Transactional
    public UserProfileDTO updateUserProfile(String email, UserUpdateDTO dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

        if (dto.getWeight() != null) user.setWeight(dto.getWeight());
        if (dto.getHeight() != null) user.setHeight(dto.getHeight());
        if (dto.getBirthDate() != null) user.setBirthDate(LocalDate.parse(dto.getBirthDate()));
        if (dto.getPlan() != null) user.setPlan(dto.getPlan());

        // Atualiza o 'goal' na tabela de anamnese
        if (dto.getGoal() != null) {
            UserAnamnesis anamnesis = anamnesisRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Anamnese não encontrada para o usuário. Crie uma antes de atualizar o objetivo."));
            anamnesis.setMainGoal(dto.getGoal());
            anamnesisRepository.save(anamnesis);
        }

        // Lógica para preferências e restrições (permanece a mesma)
        if (dto.getDietaryPreferences() != null) {
            userPreferenceRepository.deleteByUser(user);
            user.getPreferences().clear(); // Limpa a coleção na entidade
            for (String prefName : dto.getDietaryPreferences()) {
                DietaryPreference pref = preferenceRepository.findByName(prefName)
                        .orElseGet(() -> preferenceRepository.save(new DietaryPreference(null, prefName)));
                user.getPreferences().add(new UserPreference(user, pref));
            }
        }

        if (dto.getRestrictions() != null) {
            userRestrictionRepository.deleteByUser(user);
            user.getRestrictions().clear(); // Limpa a coleção na entidade
            for (String resName : dto.getRestrictions()) {
                DietaryRestriction res = restrictionRepository.findByName(resName)
                        .orElseGet(() -> restrictionRepository.save(new DietaryRestriction(null, resName)));
                user.getRestrictions().add(new UserRestriction(user, res));
            }
        }

        User savedUser = userRepository.save(user);
        return toUserProfileDTO(savedUser);
    }
    
    // ✅ HELPER ATUALIZADO para buscar o 'goal' da anamnese
    private UserProfileDTO toUserProfileDTO(User user) {
        List<String> preferences = user.getPreferences().stream()
                .map(up -> up.getPreference().getName())
                .collect(Collectors.toList());

        List<String> restrictions = user.getRestrictions().stream()
                .map(ur -> ur.getRestriction().getName())
                .collect(Collectors.toList());

        String birthDateStr = user.getBirthDate() != null ? user.getBirthDate().toString() : null;
        String roleStr = user.getRole() != null ? user.getRole().name() : null;

        // Busca o objetivo principal da anamnese associada
        String goalStr = anamnesisRepository.findByUserId(user.getId())
                .map(anamnesis -> anamnesis.getMainGoal().name())
                .orElse(null); // Retorna null se não houver anamnese

        return new UserProfileDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                roleStr,
                goalStr, // Usa o objetivo da anamnese
                user.getWeight(),
                user.getHeight(),
                birthDateStr,
                preferences,
                restrictions,
                user.getPlan(),
                user.getApproved()
        );
    }

    // --- DEMAIS MÉTODOS (login, approveUser, getAllUsers, etc.) ---
    // Nenhum ajuste necessário nos outros métodos. Eles continuam funcionando como esperado.
    // ... (cole o restante dos seus métodos aqui)
    
    public LoginResponseDTO login(UserLoginDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuário ou senha inválidos"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new RuntimeException("Usuário ou senha inválidos");
        }

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

        try {
            String subject = "Sua conta foi aprovada!";
            String text = "Olá " + user.getName() + ",\n\n" +
                    "Sua conta no NutriX foi aprovada pelo administrador. Agora você pode acessar o sistema normalmente.\n\n"
                    +
                    "Atenciosamente,\nEquipe NutriX";

            emailService.sendEmail(user.getEmail(), subject, text);
        } catch (Exception e) {
            System.err.println("❌ Erro ao enviar email, mas usuário foi aprovado: " + e.getMessage());
        }
    }

    public List<UserViewDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toUserViewDTO)
                .collect(Collectors.toList());
    }

    public UserProfileDTO getUserProfileByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));
        return toUserProfileDTO(user);
    }

    public UserProfileDTO getUserProfile(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));
        return toUserProfileDTO(user);
    }

    public boolean isSameUser(Long userId, String email) {
        return userRepository.findByEmail(email)
                .map(user -> user.getId().equals(userId))
                .orElse(false);
    }

    private UserViewDTO toUserViewDTO(User user) {
        return new UserViewDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getApproved()
        );
    }
}