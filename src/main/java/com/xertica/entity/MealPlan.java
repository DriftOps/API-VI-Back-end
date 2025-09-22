package com.xertica.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "meal_plans")
public class MealPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String title;
    private Integer totalCalories;

    @Column(columnDefinition = "jsonb")
    private String meals; // JSON com as refeições

    private Boolean active = true;
    private LocalDateTime createdAt = LocalDateTime.now();
}
