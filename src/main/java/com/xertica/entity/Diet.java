package com.xertica.entity;

import com.xertica.entity.enums.DietStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "diets")
@Getter
@Setter
public class Diet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", unique = true)
    private User user;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DietStatus status = DietStatus.ACTIVE;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private BigDecimal initialWeight;

    @Column(nullable = false)
    private BigDecimal targetWeight;

    @Column(nullable = false)
    private Integer baseDailyCalories;
    private Integer baseDailyProteinG;
    private Integer baseDailyCarbsG;
    private Integer baseDailyFatsG;

    @Column(nullable = false)
    private Integer safeMetabolicFloor; // TMB do usuário

    @Column(columnDefinition = "TEXT")
    private String aiRationale;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "diet", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<DietDailyTarget> dailyTargets = new ArrayList<>();
}