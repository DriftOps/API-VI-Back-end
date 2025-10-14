package com.xertica.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {
    private Long id;
    private String name;
    private String email;
    private String role;
    private String goal; // ✅ Este campo agora virá da Anamnese
    private Double weight;
    private Integer height;
    private String birthDate;
    private List<String> dietaryPreferences;
    private List<String> restrictions;
    private String plan;
    private Boolean approved;
}