// src/main/java/com/xertica/dto/UpdateDailyTargetDTO.java
package com.xertica.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateDailyTargetDTO {
    
    @NotNull
    @Positive(message = "Calorias devem ser um valor positivo")
    private Integer adjustedCalories;
    
    // (Opcional) Adicione macros se você permitir a edição deles
    // private Integer adjustedProteinG;
    // private Integer adjustedCarbsG;
    // private Integer adjustedFatsG;
}