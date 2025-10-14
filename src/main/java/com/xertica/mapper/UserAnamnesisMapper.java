package com.xertica.mapper;

import com.xertica.dto.UserAnamnesisDTO;
import com.xertica.entity.UserAnamnesis;

public class UserAnamnesisMapper {

    public static UserAnamnesisDTO toDTO(UserAnamnesis entity) {
        if (entity == null) return null;

        return UserAnamnesisDTO.builder()
                .id(entity.getId())
                .userId(entity.getUser().getId())
                .mainGoal(entity.getMainGoal())
                .medicalConditions(entity.getMedicalConditions())
                .allergies(entity.getAllergies())
                .surgeries(entity.getSurgeries())
                .activityType(entity.getActivityType())
                .frequency(entity.getFrequency())
                .activityMinutesPerDay(entity.getActivityMinutesPerDay())
                .sleepQuality(entity.getSleepQuality())
                .wakesDuringNight(entity.getWakesDuringNight())
                .bowelFrequency(entity.getBowelFrequency())
                .stressLevel(entity.getStressLevel())
                .alcoholUse(entity.getAlcoholUse())
                .smoking(entity.getSmoking())
                .hydrationLevel(entity.getHydrationLevel())
                .continuousMedication(entity.getContinuousMedication())
                .build();
    }

    public static UserAnamnesis toEntity(UserAnamnesisDTO dto) {
        if (dto == null) return null;

        return UserAnamnesis.builder()
                .id(dto.getId())
                .mainGoal(dto.getMainGoal())
                .medicalConditions(dto.getMedicalConditions())
                .allergies(dto.getAllergies())
                .surgeries(dto.getSurgeries())
                .activityType(dto.getActivityType())
                .frequency(dto.getFrequency())
                .activityMinutesPerDay(dto.getActivityMinutesPerDay())
                .sleepQuality(dto.getSleepQuality())
                .wakesDuringNight(dto.getWakesDuringNight())
                .bowelFrequency(dto.getBowelFrequency())
                .stressLevel(dto.getStressLevel())
                .alcoholUse(dto.getAlcoholUse())
                .smoking(dto.getSmoking())
                .hydrationLevel(dto.getHydrationLevel())
                .continuousMedication(dto.getContinuousMedication())
                .build();
    }
}
