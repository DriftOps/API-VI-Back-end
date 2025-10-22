package com.xertica.service;

import com.xertica.dto.*;
import com.xertica.entity.*;
import com.xertica.entity.enums.UserRole;
import com.xertica.entity.enums.anamnesis.ActivityTypeEnum;
import org.hibernate.Hibernate;
import java.time.LocalDate;
import java.time.Period;
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
import com.xertica.dto.context.AIContextDTO;
import com.xertica.dto.context.AnamnesisContextDTO;
import com.xertica.dto.context.UserContextDTO;

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
        if (dto.getWeight() != null)
            user.setWeight(dto.getWeight());
        if (dto.getHeight() != null)
            user.setHeight(dto.getHeight());
        if (dto.getBirthDate() != null && !dto.getBirthDate().isEmpty()) {
            user.setBirthDate(java.time.LocalDate.parse(dto.getBirthDate()));
        }

        // 2. Atualiza dados da Anamnesis
        UserAnamnesis anamnesis = userAnamnesisRepository.findByUserId(user.getId())
                .orElse(new UserAnamnesis());

        anamnesis.setUser(user);
        user.setAnamnesis(anamnesis);

        // Atualiza campos de seleção única e outros
        if (dto.getGoal() != null)
            anamnesis.setMainGoal(dto.getGoal());
        if (dto.getActivityLevel() != null)
            anamnesis.setActivityType(dto.getActivityLevel());
        if (dto.getFrequency() != null)
            anamnesis.setFrequency(dto.getFrequency());
        if (dto.getActivityMinutesPerDay() != null)
            anamnesis.setActivityMinutesPerDay(dto.getActivityMinutesPerDay());
        if (dto.getSleepQuality() != null)
            anamnesis.setSleepQuality(dto.getSleepQuality());
        if (dto.getWakesDuringNight() != null)
            anamnesis.setWakesDuringNight(dto.getWakesDuringNight());
        if (dto.getBowelFrequency() != null)
            anamnesis.setBowelFrequency(dto.getBowelFrequency());
        if (dto.getAlcoholUse() != null)
            anamnesis.setAlcoholUse(dto.getAlcoholUse());
        if (dto.getSmoking() != null)
            anamnesis.setSmoking(dto.getSmoking());
        if (dto.getHydrationLevel() != null)
            anamnesis.setHydrationLevel(dto.getHydrationLevel());
        if (dto.getContinuousMedication() != null)
            anamnesis.setContinuousMedication(dto.getContinuousMedication());

        // ✅ CORREÇÃO: Lógica simplificada para substituir os valores dos seletores
        // múltiplos
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
        String rawPassword = "pass1234";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        System.out.println("Hash para '" + rawPassword + "': " + encodedPassword);
        System.out.println("\n🔐 === INICIANDO LOGIN ===");
        System.out.println("Email: " + dto.getEmail());
        System.out.println("Senha fornecida: " + dto.getPassword());

        // Buscar usuário
        Optional<User> userOpt = userRepository.findByEmail(dto.getEmail());

        if (userOpt.isEmpty()) {
            System.out.println("❌ Usuário NÃO encontrado no BD: " + dto.getEmail());
            throw new RuntimeException("Usuário ou senha inválidos");
        }

        User user = userOpt.get();
        System.out.println("✅ Usuário encontrado no BD:");
        System.out.println("   - ID: " + user.getId());
        System.out.println("   - Nome: " + user.getName());
        System.out.println("   - Email: " + user.getEmail());
        System.out.println("   - Aprovado: " + user.getApproved());
        System.out.println("   - Role: " + user.getRole());
        System.out.println("   - Senha no BD (hash): " + user.getPassword());

        // Verificar senha
        System.out.println("🔑 Verificando senha...");
        boolean passwordMatches = passwordEncoder.matches(dto.getPassword(), user.getPassword());
        System.out.println("   - Senha confere: " + passwordMatches);

        if (!passwordMatches) {
            System.out.println("❌ Senha NÃO confere");
            System.out.println("   - Senha fornecida: " + dto.getPassword());
            System.out.println("   - Hash no BD: " + user.getPassword());
            throw new RuntimeException("Usuário ou senha inválidos");
        }

        // Verificar aprovação
        System.out.println("📋 Verificando aprovação...");
        System.out.println("   - Aprovado: " + user.getApproved());
        System.out.println("   - É ADMIN: " + (user.getRole() == UserRole.ADMIN));

        if (!user.getApproved() && user.getRole() != UserRole.ADMIN) {
            System.out.println("❌ Usuário não aprovado");
            throw new RuntimeException("Usuário ainda não aprovado pelo administrador.");
        }

        System.out.println("✅ Credenciais válidas, gerando token...");

        // GERAR TOKEN
        String token = JwtUtils.generateToken(user.getEmail(), user.getId());
        System.out.println("✅ Token gerado com sucesso!");
        System.out.println("   - Token (início): " + token.substring(0, Math.min(20, token.length())) + "...");

        // Criar response
        LoginResponseDTO response = new LoginResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                token);

        System.out.println("✅ LoginResponseDTO criado com sucesso!");
        System.out.println("🔐 === LOGIN CONCLUÍDO ===\n");

        return response;
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
                user.getApproved());
    }

    @Transactional(readOnly = true)
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com o e-mail: " + email));
    }

    @Transactional(readOnly = true)
    public AIContextDTO getUserContextForAI(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com o e-mail: " + email));

        UserAnamnesis anamnesis = user.getAnamnesis();

        // Mapeia os dados do usuário para o DTO - FORMA CORRIGIDA
        UserContextDTO userDTO = new UserContextDTO();
        userDTO.setName(user.getName());
        userDTO.setWeight(user.getWeight());

        // Converte a altura para Double, caso seja Integer na entidade
        if (user.getHeight() != null) {
            userDTO.setHeight(user.getHeight().doubleValue());
        }
        if (user.getBirthDate() != null) {
            userDTO.setAge(Period.between(user.getBirthDate(), LocalDate.now()).getYears());
        }
        if (user.getGender() != null) {
            userDTO.setGender(user.getGender()); // Gênero PODE ser um Enum, então mantemos .name() aqui.
        }

        // Mapeia os dados da anamnese para o DTO (já estava correto)
        AnamnesisContextDTO anamnesisDTO = new AnamnesisContextDTO();
        if (anamnesis != null) {
            anamnesisDTO.setMainGoal(anamnesis.getMainGoal() != null ? anamnesis.getMainGoal().name() : "N/A");
            anamnesisDTO.setMedicalConditions(anamnesis.getMedicalConditions());
            anamnesisDTO.setAllergies(anamnesis.getAllergies());
            anamnesisDTO.setSurgeries(anamnesis.getSurgeries());
            anamnesisDTO
                    .setActivityType(anamnesis.getActivityType() != null ? anamnesis.getActivityType().name() : "N/A");
            anamnesisDTO.setFrequency(anamnesis.getFrequency() != null ? anamnesis.getFrequency().name() : "N/A");
            anamnesisDTO.setActivityMinutesPerDay(anamnesis.getActivityMinutesPerDay());
            anamnesisDTO
                    .setSleepQuality(anamnesis.getSleepQuality() != null ? anamnesis.getSleepQuality().name() : "N/A");
            anamnesisDTO.setWakesDuringNight(
                    anamnesis.getWakesDuringNight() != null ? anamnesis.getWakesDuringNight().name() : "N/A");
            anamnesisDTO.setBowelFrequency(
                    anamnesis.getBowelFrequency() != null ? anamnesis.getBowelFrequency().name() : "N/A");
            anamnesisDTO.setStressLevel(anamnesis.getStressLevel() != null ? anamnesis.getStressLevel().name() : "N/A");
            anamnesisDTO.setAlcoholUse(anamnesis.getAlcoholUse() != null ? anamnesis.getAlcoholUse().name() : "N/A");
            anamnesisDTO.setSmoking(anamnesis.getSmoking());
            anamnesisDTO.setHydrationLevel(
                    anamnesis.getHydrationLevel() != null ? anamnesis.getHydrationLevel().name() : "N/A");
            anamnesisDTO.setContinuousMedication(anamnesis.getContinuousMedication());
        }

        return new AIContextDTO(userDTO, anamnesisDTO);
    }

}