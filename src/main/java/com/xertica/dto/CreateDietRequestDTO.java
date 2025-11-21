package com.xertica.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateDietRequestDTO {

    @NotNull(message = "O ID do usuário é obrigatório")
    private Long userId;

    @NotNull(message = "O título é obrigatório")
    private String title;

    @NotNull(message = "A data final é obrigatória")
    @Future(message = "A data final deve ser no futuro")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @NotNull(message = "O peso alvo é obrigatório")
    @Min(value = 0, message = "O peso deve ser positivo")
    private BigDecimal targetWeight;

    @NotNull(message = "A meta de calorias é obrigatória")
    private Integer baseDailyCalories;

    private Integer baseDailyProteinG;
    private Integer baseDailyCarbsG;
    private Integer baseDailyFatsG;

    private String aiRationale;

    @NotNull(message = "O piso metabólico é obrigatório")
    private Integer safeMetabolicFloor;
}