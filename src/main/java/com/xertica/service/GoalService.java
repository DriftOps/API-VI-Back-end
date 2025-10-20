package com.xertica.service;

import com.xertica.dto.GoalDTO;
import com.xertica.entity.Goal;
import com.xertica.entity.User;
import com.xertica.repository.GoalRepository;
import com.xertica.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GoalService {

    private final GoalRepository goalRepository;
    private final UserRepository userRepository;

    // Criar nova meta
    @Transactional
    public GoalDTO createGoal(GoalDTO dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Goal goal = Goal.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .deadline(LocalDate.parse(dto.getDeadline()))
                .progress(dto.getProgress() != null ? dto.getProgress() : 0.0)
                .user(user)
                .build();

        goalRepository.save(goal);
        return toDTO(goal);
    }

    // Buscar metas por usuário
    public List<GoalDTO> getGoalsByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        return goalRepository.findByUser(user)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // Atualizar progresso
    @Transactional
    public GoalDTO updateProgress(Long id, Double newProgress) {
        Goal goal = goalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Meta não encontrada"));

        goal.setProgress(newProgress);
        goalRepository.save(goal);

        return toDTO(goal);
    }

    // Calcular progresso médio de todas as metas do usuário
    public Double calculateAverageProgress(Long userId) {
        List<GoalDTO> goals = getGoalsByUser(userId);
        if (goals.isEmpty()) return 0.0;
        return goals.stream()
                .mapToDouble(GoalDTO::getProgress)
                .average()
                .orElse(0.0);
    }

    private GoalDTO toDTO(Goal goal) {
        return new GoalDTO(
                goal.getId(),
                goal.getTitle(),
                goal.getDescription(),
                goal.getDeadline().toString(),
                goal.getProgress(),
                goal.getUser().getId()
        );
    }
}
