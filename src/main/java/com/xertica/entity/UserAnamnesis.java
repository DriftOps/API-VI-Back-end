package com.xertica.entity;

import com.xertica.entity.enums.anamnesis.ActivityTypeEnum;
import com.xertica.entity.enums.anamnesis.AlcoholUseType;
import com.xertica.entity.enums.anamnesis.BowelFrequencyType;
import com.xertica.entity.enums.anamnesis.FrequencyType;
import com.xertica.entity.enums.anamnesis.HydrationLevelType;
import com.xertica.entity.enums.anamnesis.MainGoalType;
import com.xertica.entity.enums.anamnesis.SleepQualityType;
import com.xertica.entity.enums.anamnesis.StressLevelType;
import com.xertica.entity.enums.anamnesis.WakesDuringNightType;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_anamnesis")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAnamnesis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column
    private MainGoalType mainGoal;

    @Column(columnDefinition = "TEXT")
    private String medicalConditions;

    @Column(columnDefinition = "TEXT")
    private String allergies;

    @Column(columnDefinition = "TEXT")
    private String surgeries;

    @Enumerated(EnumType.STRING)
    private ActivityTypeEnum activityType;

    @Enumerated(EnumType.STRING)
    private FrequencyType frequency;

    private Integer activityMinutesPerDay;

    @Enumerated(EnumType.STRING)
    private SleepQualityType sleepQuality;

    @Enumerated(EnumType.STRING)
    private WakesDuringNightType wakesDuringNight;

    @Enumerated(EnumType.STRING)
    private BowelFrequencyType bowelFrequency;

    @Enumerated(EnumType.STRING)
    private StressLevelType stressLevel;

    @Enumerated(EnumType.STRING)
    private AlcoholUseType alcoholUse;

    @Builder.Default
    private Boolean smoking = false;

    @Enumerated(EnumType.STRING)
    private HydrationLevelType hydrationLevel;

    @Builder.Default
    private Boolean continuousMedication = false;
}