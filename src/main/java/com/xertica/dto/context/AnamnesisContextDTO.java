package com.xertica.dto.context;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnamnesisContextDTO {
    private String mainGoal;
    private String medicalConditions;
    private String allergies;
    private String surgeries;
    private String activityType;
    private String frequency;
    private Integer activityMinutesPerDay;
    private String sleepQuality;
    private String wakesDuringNight;
    private String bowelFrequency;
    private String stressLevel;
    private String alcoholUse;
    private Boolean smoking;
    private String hydrationLevel;
    private Boolean continuousMedication;
}