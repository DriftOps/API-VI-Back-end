package com.xertica.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Data
public class MealDTO {
    private Long id;
    private String type;
    private String description;
    private Double calories;
    private Double protein;
    private Double carbs;
    private Double fat;
    private LocalDateTime createdAt;
    private LocalDate mealDate;

}