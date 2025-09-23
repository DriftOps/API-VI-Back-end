package com.xertica.dto;

import com.xertica.entity.enums.UserRole;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {
    private Long id;
    private String name;
    private String email;
    private UserRole role;
    private String token;
}