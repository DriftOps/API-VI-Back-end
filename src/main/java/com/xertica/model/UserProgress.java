package com.xertica.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_progress")
public class UserProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private Double weight;          // peso em kg
    private Double bodyFatPercent;  // percentual de gordura
    private Double muscleMass;      // massa muscular

    private LocalDateTime createdAt = LocalDateTime.now();
}
