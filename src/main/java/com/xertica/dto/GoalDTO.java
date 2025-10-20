package com.xertica.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoalDTO {
    private Long id;
    private String title;
    private String description;
    private String deadline; // formato "yyyy-MM-dd"
    private Double progress;
    private Long userId;
}
