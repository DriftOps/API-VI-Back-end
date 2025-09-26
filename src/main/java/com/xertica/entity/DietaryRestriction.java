package com.xertica.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "dietary_restrictions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DietaryRestriction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
}