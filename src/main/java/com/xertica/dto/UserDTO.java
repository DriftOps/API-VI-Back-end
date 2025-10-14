package com.xertica.dto;

import com.xertica.entity.enums.UserRole;
import lombok.*;

import java.time.LocalDate;

/**
 * DTO simplificado para o processo de signup e criação de usuário.
 * Contém apenas os dados essenciais da conta.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    private String name;
    private String email;
    private String password;
    private UserRole role; // Usado principalmente para criação via admin
    private Integer height;
    private Double weight;
    private LocalDate birthDate;
    private String gender;
}