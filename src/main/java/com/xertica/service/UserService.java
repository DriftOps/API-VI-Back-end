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

import java.util.ArrayList;
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

    // Criar usuário (admin/geral)
    @Transactional
    public UserViewDTO createUser(UserDTO dto) {
         List<Object> chatHistory = dto.getChatHistory() != null ? dto.getChatHistory() : new ArrayList<>();

        User user = User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(dto.getRole() != null ? dto.getRole() : UserRole.CLIENT)
                .goal(dto.getGoal())
                .height(dto.getHeight())
                .weight(dto.getWeight())
                .birthDate(dto.getBirthDate())
                .activityLevel(dto.getActivityLevel())
                .chatHistory(dto.getChatHistory())
                .plan(dto.getPlan())
                .build();

        userRepository.save(user);

        // Salva preferências
        if (dto.getPreferences() != null) {
            for (String prefName : dto.getPreferences()) {
                DietaryPreference pref = preferenceRepository.findByName(prefName)
                        .orElseGet(() -> preferenceRepository.save(new DietaryPreference(null, prefName)));
                userPreferenceRepository.save(new UserPreference(user, pref));
            }
        }

        // Salva restrições
        if (dto.getRestrictions() != null) {
            for (String resName : dto.getRestrictions()) {
                DietaryRestriction res = restrictionRepository.findByName(resName)
                        .orElseGet(() -> restrictionRepository.save(new DietaryRestriction(null, resName)));
                userRestrictionRepository.save(new UserRestriction(user, res));
            }
        }

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
    public UserViewDTO signup(UserCreateDTO dto) {
        UserDTO userDto = dtoToUserDTO(dto);
        return createUser(userDto);
    }

    // Login
    public LoginResponseDTO login(UserLoginDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuário ou senha inválidos"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new RuntimeException("Usuário ou senha inválidos");
        }

        String token = JwtUtils.generateToken(user.getEmail(), user.getId());

        return new LoginResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                token
        );
    }

    // ===== Helpers =====
    private UserViewDTO toUserViewDTO(User user) {
        return new UserViewDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }

    private UserDTO dtoToUserDTO(UserCreateDTO dto) {
        return new UserDTO(
                dto.getName(),
                dto.getEmail(),
                dto.getPassword(),
                dto.getRole(),
                dto.getGoal(),
                dto.getHeight(),
                dto.getWeight(),
                dto.getBirthDate(),
                dto.getActivityLevel(),
                dto.getPreferences(),
                dto.getRestrictions(),
                null,
                null
        );
    }
}
