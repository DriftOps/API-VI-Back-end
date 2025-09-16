package com.xertica.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.CLIENT;  // novo campo

    private String goal;
    private Integer height;
    private Double weight;
    private LocalDate birthDate;

    @Column(updatable = false)
    private LocalDate createdAt = LocalDate.now();

    private String activityLevel;
    private String gender;
    private String timezone;
    private String language = "pt";
    private Boolean onboardingCompleted = false;
    private Boolean aiAssistantEnabled = true;
    private LocalDateTime lastLogin;

    // Relacionamentos
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_preferences",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "preference_id")
    )
    private Set<DietaryPreference> preferences;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_restrictions",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "restriction_id")
    )
    private Set<DietaryRestriction> restrictions;

    @Column(columnDefinition = "jsonb")
    private String chatHistory;

    @Column(columnDefinition = "jsonb")
    private String plan;
}
