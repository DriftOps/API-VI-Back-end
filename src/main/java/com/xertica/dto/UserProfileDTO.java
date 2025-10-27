package com.xertica.dto;

import com.xertica.entity.enums.anamnesis.*; // Importar todos os enums
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {
    private Long id;
    private String name;
    private String email;
    private String role;
    private Double weight;
    private Integer height;
    private String birthDate;
    private List<String> dietaryPreferences;
    private List<String> restrictions;
    private Boolean approved;
    private String plan;
    
    // --- Campos da Anamnese ---
    private String goal; // Já existia
    private ActivityTypeEnum activityLevel; // Já existia
    private String medicalConditions;
    private String allergies;
    private String surgeries;
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