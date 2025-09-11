package com.xertica.model;

import jakarta.persistence.*;
import lombok.*;

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
    private Double height;
    private Double weight;
    private Integer age;

    @Column(columnDefinition = "text[]")
    private String[] restrictions;   // Ex: {"lactose", "glúten"}

    private String activityLevel;

    @Column(columnDefinition = "text[]")
    private String[] dietaryPreferences; // Ex: {"low-carb", "vegano"}

    @Column(columnDefinition = "jsonb")
    private String chatHistory;   // JSON bruto armazenado em string

    @Column(columnDefinition = "jsonb")
    private String plan;          // JSON bruto armazenado em string
}
