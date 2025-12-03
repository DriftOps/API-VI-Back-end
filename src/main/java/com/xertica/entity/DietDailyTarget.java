package com.xertica.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "diet_daily_targets", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"diet_id", "target_date"})
})
@Getter
@Setter
public class DietDailyTarget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diet_id", nullable = false)
    private Diet diet;

    @Column(nullable = false)
    private LocalDate targetDate;

    @Column(nullable = false)
    private Integer adjustedCalories;
    private Integer adjustedProteinG;
    private Integer adjustedCarbsG;
    private Integer adjustedFatsG;

    private Integer consumedCalories = 0;
    private BigDecimal consumedProteinG = BigDecimal.ZERO;
    private BigDecimal consumedCarbsG = BigDecimal.ZERO;
    private BigDecimal consumedFatsG = BigDecimal.ZERO;

    private LocalDateTime lastUpdated = LocalDateTime.now();

    @Column(columnDefinition = "TEXT")
    private String suggestedMenu;
}