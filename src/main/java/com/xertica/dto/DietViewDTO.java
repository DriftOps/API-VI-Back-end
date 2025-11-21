package com.xertica.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class DietViewDTO {
    private Long id;
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal initialWeight;
    private BigDecimal targetWeight;
    
    private Integer baseDailyCalories;
    
    private Integer baseDailyProteinG;
    private Integer baseDailyCarbsG;
    private Integer baseDailyFatsG;
    private Integer safeMetabolicFloor;

    private String aiRationale;
    private List<DietDailyTargetDTO> dailyTargets;
}