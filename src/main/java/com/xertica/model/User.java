package com.xertica.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String goal;

    private Integer height;       // em cm
    private Double weight;        // em kg

    private LocalDate birthDate;  // nova data de nascimento

    @Column(updatable = false)
    private LocalDate createdAt = LocalDate.now();  // data de cadastro

    @Column(columnDefinition = "text[]")
    private String[] restrictions;

    private String activityLevel;

    @Column(columnDefinition = "text[]")
    private String[] dietaryPreferences;

    @Column(columnDefinition = "jsonb")
    private String chatHistory;

    @Column(columnDefinition = "jsonb")
    private String plan;
}
