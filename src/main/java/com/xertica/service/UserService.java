package com.xertica.service;

import com.xertica.dto.*;
import com.xertica.entity.*;
import com.xertica.entity.enums.UserRole;
import com.xertica.entity.enums.anamnesis.ActivityTypeEnum;
import com.xertica.mapper.UserMapper;
import com.xertica.repository.*;
import com.xertica.security.JwtUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserAnamnesisRepository userAnamnesisRepository;
    private final DietaryPreferenceRepository preferenceRepository;
    private final DietaryRestrictionRepository restrictionRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final UserRestrictionRepository userRestrictionRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    // ✅ MÉTODO CORRIGIDO E UNIFICADO
    @Transactional
public UserProfileDTO updateUserProfile(String email, UserUpdateDTO dto) {
    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

    // 1. Atualiza dados do User
    if (dto.getWeight() != null) user.setWeight(dto.getWeight());
    if (dto.getHeight() != null) user.setHeight(dto.getHeight());
    if (dto.getBirthDate() != null && !dto.getBirthDate().isEmpty()) {
        user.setBirthDate(java.time.LocalDate.parse(dto.getBirthDate()));
    }

    // 2. Atualiza dados da Anamnesis
    UserAnamnesis anamnesis = userAnamnesisRepository.findByUserId(user.getId())
            .orElse(new UserAnamnesis());

    anamnesis.setUser(user);
    user.setAnamnesis(anamnesis);
    
    // Atualiza campos de seleção única e outros
    if (dto.getGoal() != null) anamnesis.setMainGoal(dto.getGoal());
    if (dto.getActivityLevel() != null) anamnesis.setActivityType(dto.getActivityLevel());
    if (dto.getFrequency() != null) anamnesis.setFrequency(dto.getFrequency());
    if (dto.getActivityMinutesPerDay() != null) anamnesis.setActivityMinutesPerDay(dto.getActivityMinutesPerDay());
    if (dto.getSleepQuality() != null) anamnesis.setSleepQuality(dto.getSleepQuality());
    if (dto.getWakesDuringNight() != null) anamnesis.setWakesDuringNight(dto.getWakesDuringNight());
    if (dto.getBowelFrequency() != null) anamnesis.setBowelFrequency(dto.getBowelFrequency());
    if (dto.getAlcoholUse() != null) anamnesis.setAlcoholUse(dto.getAlcoholUse());
    if (dto.getSmoking() != null) anamnesis.setSmoking(dto.getSmoking());
    if (dto.getHydrationLevel() != null) anamnesis.setHydrationLevel(dto.getHydrationLevel());
    if (dto.getContinuousMedication() != null) anamnesis.setContinuousMedication(dto.getContinuousMedication());
    
    // ✅ CORREÇÃO: Lógica simplificada para substituir os valores dos seletores múltiplos
    // Como o front-end envia a lista completa, apenas salvamos o novo valor.
    if (dto.getMedicalConditions() != null) {
        anamnesis.setMedicalConditions(dto.getMedicalConditions());
    }
    if (dto.getAllergies() != null) {
        anamnesis.setAllergies(dto.getAllergies());
    }
    if (dto.getSurgeries() != null) {
        anamnesis.setSurgeries(dto.getSurgeries());
    }

    // 3. Atualiza preferências e restrições (se aplicável)
    if (dto.getDietaryPreferences() != null) {
        userPreferenceRepository.deleteByUser(user);
        user.getPreferences().clear();
        for (String prefName : dto.getDietaryPreferences()) {
            DietaryPreference pref = preferenceRepository.findByName(prefName)
                    .orElseGet(() -> preferenceRepository.save(new DietaryPreference(null, prefName)));
            user.getPreferences().add(new UserPreference(user, pref));
        }
    }
    if (dto.getRestrictions() != null) {
        userRestrictionRepository.deleteByUser(user);
        user.getRestrictions().clear();
        for (String resName : dto.getRestrictions()) {
            DietaryRestriction res = restrictionRepository.findByName(resName)
                    .orElseGet(() -> restrictionRepository.save(new DietaryRestriction(null, resName)));
            user.getRestrictions().add(new UserRestriction(user, res));
        }
    }

    // 4. Salva e retorna
    User updatedUser = userRepository.save(user);
    return UserMapper.toUserProfileDTO(updatedUser);
}

    // --- DEMAIS MÉTODOS (signup, login, etc. sem alterações) ---
    @Transactional
    public UserViewDTO signup(UserDTO dto) {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalStateException("O e-mail informado já está em uso.");
        }

        User user = User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(UserRole.CLIENT)
                .height(dto.getHeight())
                .weight(dto.getWeight())
                .birthDate(dto.getBirthDate())
                .gender(dto.getGender())
                .approved(false)
                .build();

        userRepository.save(user);
        return toUserViewDTO(user);
    }
    
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
                .approved(true)
                .build();

        userRepository.save(user);
        return toUserViewDTO(user);
    }
    
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

    // ✅ CORREÇÃO: Removidas as definições duplicadas. Apenas uma de cada método.
    @Transactional(readOnly = true)
    public UserProfileDTO getUserProfileByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com o e-mail: " + email));
        return UserMapper.toUserProfileDTO(user);
    }

    @Transactional(readOnly = true)
    public UserProfileDTO getUserProfile(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com o ID: " + id));
        return UserMapper.toUserProfileDTO(user);
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