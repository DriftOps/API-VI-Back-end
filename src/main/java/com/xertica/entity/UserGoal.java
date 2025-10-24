package com.xertica.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_goals")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relação com o usuário
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private Double targetWeight;     // meta de peso
    private Double initialWeight;    // peso inicial
    private Double currentWeight;    // peso atual no momento do registro
    private String description;      // ex: "Perder 5kg até Dezembro"
    private LocalDateTime deadline;  // data limite
    private LocalDateTime createdAt; // data de criação

    private Double progress; // percentual de progresso
}
