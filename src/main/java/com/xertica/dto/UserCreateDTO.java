package com.xertica.dto;

import com.xertica.entity.enums.UserRole;
import com.xertica.entity.enums.GoalType;
import com.xertica.entity.enums.ActivityLevelType;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreateDTO {
    private String name;
    private String email;
    private String password;
    private UserRole role;
    private GoalType goal;
    private Integer height;
    private Double weight;
    private LocalDate birthDate;
    private ActivityLevelType activityLevel;
    private List<String> preferences;
    private List<String> restrictions;
}