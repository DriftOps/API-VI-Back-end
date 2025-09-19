package com.xertica.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "faqs")
public class Faq {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private String question;

    @Column(nullable=false, columnDefinition = "TEXT")
    private String answer;

    // Ex.: "Almoço;Janta"
    private String tags;
}