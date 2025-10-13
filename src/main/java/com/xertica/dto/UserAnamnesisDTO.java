package com.xertica.dto;

import com.xertica.entity.enums.anamnesis.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAnamnesisDTO {

    private Long id;
    private Long userId;

    private MainGoalType mainGoal;
    private String medicalConditions;
    private String allergies;
    private String surgeries;

    private ActivityTypeEnum activityType;
    private FrequencyType frequency;
    private Integer activityMinutesPerDay;

    private SleepQualityType sleepQuality;
    private WakesDuringNightType wakesDuringNight;

    private BowelFrequencyType bowelFrequency;
    private StressLevelType stressLevel;
    private AlcoholUseType alcoholUse;

    private Boolean smoking;
    private HydrationLevelType hydrationLevel;
    private Boolean continuousMedication;
}