package com.nutrix.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_pipeline")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPipeline {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "step_order")
    private int stepOrder;

    @Column(name = "step_name", nullable = false)
    private String stepName;

    private String description;

    private boolean completed;

    private LocalDateTime completedAt;
}
