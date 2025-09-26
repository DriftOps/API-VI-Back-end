package com.xertica.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "dietary_preferences")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DietaryPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
}