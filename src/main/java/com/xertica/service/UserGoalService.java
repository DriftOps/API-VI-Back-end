package com.xertica.service;

import com.xertica.dto.UserGoalDTO;
import com.xertica.entity.User;
import com.xertica.entity.UserGoal;
import com.xertica.mapper.UserGoalMapper;
import com.xertica.repository.UserGoalRepository;
import com.xertica.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserGoalService {

    private final UserGoalRepository goalRepository;
    private final UserRepository userRepository;

    public List<UserGoalDTO> getUserGoals(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        return goalRepository.findByUserId(user.getId())
                .stream()
                .map(UserGoalMapper::toDTO)
                .collect(Collectors.toList());
    }

    public UserGoalDTO createGoal(String email, UserGoalDTO dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        UserGoal goal = UserGoalMapper.toEntity(dto);
        goal.setUser(user);
        goal.setCreatedAt(LocalDateTime.now());
        goal.setProgress(calculateProgress(dto));

        goalRepository.save(goal);
        return UserGoalMapper.toDTO(goal);
    }

    private double calculateProgress(UserGoalDTO dto) {
        if (dto.getInitialWeight() == null || dto.getTargetWeight() == null || dto.getCurrentWeight() == null) {
            return 0;
        }
        double total = Math.abs(dto.getInitialWeight() - dto.getTargetWeight());
        double done = Math.abs(dto.getInitialWeight() - dto.getCurrentWeight());
        return Math.min(100.0, (done / total) * 100.0);
    }
}
