package com.xertica.entity;

import com.xertica.entity.enums.*;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "anamneses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Anamnese {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MainReason mainReason;

    @Column(columnDefinition = "text[]")
    private String[] conditions;

    @Column(columnDefinition = "text[]")
    private String[] allergies;

    @Column(columnDefinition = "text[]")
    private String[] surgeries;

    @Enumerated(EnumType.STRING)
    private ActivityType activityType;

    @Enumerated(EnumType.STRING)
    private FrequencyType activityFrequency;

    @Enumerated(EnumType.STRING)
    private MinutesType activityMinutes;

    @Enumerated(EnumType.STRING)
    private SleepQuality sleepQuality;

    @Enumerated(EnumType.STRING)
    private NightAwakenings nightAwakenings;

    @Enumerated(EnumType.STRING)
    private EvacuationFrequency evacuationFrequency;

    @Enumerated(EnumType.STRING)
    private StressLevel stressLevel;

    @Enumerated(EnumType.STRING)
    private AlcoholConsumption alcoholConsumption;

    private Boolean smoking;
    private Boolean medicationUse;

    @Enumerated(EnumType.STRING)
    private HydrationLevel hydrationLevel;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
