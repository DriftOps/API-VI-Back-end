package com.xertica.entity;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import com.xertica.entity.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;
import com.xertica.dto.AddressDTO; // ✅ Importe o DTO

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String password;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private UserRole role = UserRole.CLIENT;

    private Integer height;
    private Double weight;
    private LocalDate birthDate;
    private String gender;

    @Column(columnDefinition = "jsonb")
    @Type(JsonBinaryType.class)
    private String chatHistory;

    @Column(columnDefinition = "jsonb")
    @Type(JsonBinaryType.class)
    private String plan;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserRestriction> restrictions = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserPreference> preferences = new ArrayList<>();

    @Column(nullable = false)
    @Builder.Default
    private Boolean approved = false;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private UserAnamnesis anamnesis;

    
    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private AddressDTO address; 
}