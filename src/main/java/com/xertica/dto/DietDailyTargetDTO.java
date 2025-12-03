package com.xertica.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class DietDailyTargetDTO {
    private Long id;
    private LocalDate targetDate;
    private Integer adjustedCalories;
    private Integer adjustedProteinG;
    private Integer adjustedCarbsG;
    private Integer adjustedFatsG;
    private Integer consumedCalories;
    private BigDecimal consumedProteinG;
    private BigDecimal consumedCarbsG;
    private BigDecimal consumedFatsG;
    private String suggestedMenu;
    
}