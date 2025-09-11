package com.xertica.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

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

    private String password;

    private String goal; // ex: "emagrecer", "ganhar massa"

    private Double height; // altura em cm
    private Double weight; // peso em kg
    private Integer age;   // idade

    @ElementCollection
    private List<String> restrictions; // ex: lactose, glúten

    private String activityLevel; // sedentário, moderado, ativo

    @ElementCollection
    private List<String> dietaryPreferences; // ex: vegano, low-carb

    @Lob
    private String chatHistory; 
    // Você pode salvar como JSON em string, ou criar uma tabela separada

    @Lob
    private String plan;
    // Também pode salvar em JSON, ou modelar em outra entidade
}
