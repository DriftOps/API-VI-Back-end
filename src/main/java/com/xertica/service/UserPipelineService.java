package com.nutrix.service;

import com.nutrix.model.User;
import com.nutrix.model.UserPipeline;
import com.nutrix.repository.UserPipelineRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserPipelineService {

    private final UserPipelineRepository repo;

    public UserPipelineService(UserPipelineRepository repo) {
        this.repo = repo;
    }

    public UserPipeline addStep(User user, String stepName, String description, int order) {
        UserPipeline step = new UserPipeline();
        step.setUser(user);
        step.setStepOrder(order);
        step.setStepName(stepName);
        step.setDescription(description);
        step.setCompleted(false);
        return repo.save(step);
    }

    public void completeStep(User user, Long stepId) {
        UserPipeline step = repo.findById(stepId)
                .orElseThrow(() -> new RuntimeException("Etapa não encontrada"));

        if (!step.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Essa etapa não pertence ao usuário!");
        }

        step.setCompleted(true);
        step.setCompletedAt(LocalDateTime.now());
        repo.save(step);
    }

    public List<UserPipeline> getUserPipeline(User user) {
        return repo.findByUserOrderByStepOrder(user);
    }

    public double getProgressPercentage(User user) {
        List<UserPipeline> steps = repo.findByUserOrderByStepOrder(user);
        if (steps.isEmpty()) return 0.0;

        long completed = steps.stream().filter(UserPipeline::isCompleted).count();
        return (completed * 100.0) / steps.size();
    }
}
