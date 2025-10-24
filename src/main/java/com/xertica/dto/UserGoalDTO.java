package com.xertica.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserGoalDTO {
    private Long id;
    private Double targetWeight;
    private Double initialWeight;
    private Double currentWeight;
    private String description;
    private String deadline;
    private Double progress;
}
