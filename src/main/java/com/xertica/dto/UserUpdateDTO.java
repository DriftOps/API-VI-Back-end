package com.xertica.dto;

import com.xertica.entity.enums.anamnesis.*; // Importar todos os enums
import lombok.Data;
import java.util.List;

@Data
public class UserUpdateDTO {
    // --- Campos User ---
    private Double weight;
    private Integer height;
    private String birthDate;
    private List<String> dietaryPreferences;
    private List<String> restrictions;
    private String plan;

    // --- Campos Anamnesis ---
    private MainGoalType goal;
    private ActivityTypeEnum activityLevel;
    private String medicalConditions; // Para 'add-only', o front enviará a nova condição a ser adicionada
    private String allergies;
    private String surgeries; // Para 'add-only', o front enviará a nova cirurgia a ser adicionada
    private FrequencyType frequency;
    private Integer activityMinutesPerDay;
    private SleepQualityType sleepQuality;
    private WakesDuringNightType wakesDuringNight;
    private BowelFrequencyType bowelFrequency;
    private AlcoholUseType alcoholUse;
    private Boolean smoking;
    private HydrationLevelType hydrationLevel;
    private Boolean continuousMedication;
    
}