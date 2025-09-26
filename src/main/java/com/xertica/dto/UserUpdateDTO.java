package com.xertica.dto;

import lombok.Data;
import java.util.List;

@Data
public class UserUpdateDTO {
    private String goal;        // String (frontend envia como string)
    private Double weight;
    private Integer height;
    private String birthDate;   // String no formato "YYYY-MM-DD"
    private String activityLevel; // String (frontend envia como string)
    private List<String> dietaryPreferences;
    private List<String> restrictions;
    private String plan;
}