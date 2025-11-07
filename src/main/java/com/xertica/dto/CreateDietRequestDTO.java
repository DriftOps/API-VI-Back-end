package com.xertica.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class CreateDietRequestDTO {
    @NotNull
    private Long userId; // Em prod, pegar do token, mas para chat pode vir do admin

    @NotEmpty
    private String title;

    @NotNull
    @Future(message = "A data final deve ser no futuro")
    private LocalDate endDate;

    @NotNull
    @Positive(message = "Peso alvo deve ser positivo")
    private BigDecimal targetWeight;

    // A IA deve pré-calcular isso antes de chamar o create
    @NotNull
    @Positive
    private Integer baseDailyCalories;

    @NotNull
    @Positive
    private Integer safeMetabolicFloor;
}