package com.xertica.dto;

import com.xertica.entity.enums.anamnesis.MainGoalType;
import lombok.Data;
import java.util.List;

@Data
public class UserUpdateDTO {
    private MainGoalType goal; // ✅ Tipo atualizado para o ENUM correto
    private Double weight;
    private Integer height;
    private String birthDate;   // String no formato "YYYY-MM-DD"
    private List<String> dietaryPreferences;
    private List<String> restrictions;
    private String plan;
}