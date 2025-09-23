package com.xertica.service;

import com.xertica.dto.UserCreateDTO;
import com.xertica.dto.UserDTO;
import com.xertica.dto.UserLoginDTO;
import com.xertica.dto.UserViewDTO;
import com.xertica.entity.*;
import com.xertica.entity.enums.UserRole;
import com.xertica.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final DietaryPreferenceRepository preferenceRepository;
    private final DietaryRestrictionRepository restrictionRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final UserRestrictionRepository userRestrictionRepository;

    @Transactional
    public User createUser(UserDTO dto) {
        // 1. Cria usuário
        User user = User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .password(dto.getPassword())
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

        // 2. Vincular preferências
        if (dto.getPreferences() != null) {
            for (String prefName : dto.getPreferences()) {
                DietaryPreference pref = preferenceRepository.findByName(prefName)
                        .orElseGet(() -> preferenceRepository.save(new DietaryPreference(null, prefName)));
                userPreferenceRepository.save(new UserPreference(user, pref));
            }
        }

        // 3. Vincular restrições
        if (dto.getRestrictions() != null) {
            for (String resName : dto.getRestrictions()) {
                DietaryRestriction res = restrictionRepository.findByName(resName)
                        .orElseGet(() -> restrictionRepository.save(new DietaryRestriction(null, resName)));
                userRestrictionRepository.save(new UserRestriction(user, res));
            }
        }

        return user;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public UserViewDTO login(UserLoginDTO dto) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'login'");
    }

    public UserViewDTO signup(UserCreateDTO dto) {
    // converter UserCreateDTO para UserDTO (pode ser direto se os campos forem iguais)
    UserDTO userDto = new UserDTO(
            dto.getName(),
            dto.getEmail(),
            dto.getPassword(), // aqui depois dá pra aplicar hash
            dto.getRole(),
            dto.getGoal(),
            dto.getHeight(),
            dto.getWeight(),
            dto.getBirthDate(),
            dto.getActivityLevel(),
            dto.getPreferences(),
            dto.getRestrictions(),
            null, // chatHistory inicial
            null  // plan inicial
    );

    User user = createUser(userDto);

    // Retornar view DTO
    return new UserViewDTO(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getRole()
    );
}

}