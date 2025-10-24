package com.xertica.mapper;

import com.xertica.dto.UserGoalDTO;
import com.xertica.entity.UserGoal;

import java.time.format.DateTimeFormatter;

public class UserGoalMapper {

    public static UserGoalDTO toDTO(UserGoal goal) {
        if (goal == null) return null;

        return UserGoalDTO.builder()
                .id(goal.getId())
                .targetWeight(goal.getTargetWeight())
                .initialWeight(goal.getInitialWeight())
                .currentWeight(goal.getCurrentWeight())
                .description(goal.getDescription())
                .deadline(goal.getDeadline() != null 
                    ? goal.getDeadline().format(DateTimeFormatter.ISO_DATE)
                    : null)
                .progress(goal.getProgress())
                .build();
    }

    public static UserGoal toEntity(UserGoalDTO dto) {
        if (dto == null) return null;

        UserGoal goal = new UserGoal();
        goal.setId(dto.getId());
        goal.setTargetWeight(dto.getTargetWeight());
        goal.setInitialWeight(dto.getInitialWeight());
        goal.setCurrentWeight(dto.getCurrentWeight());
        goal.setDescription(dto.getDescription());
        goal.setProgress(dto.getProgress());
        return goal;
    }
}
