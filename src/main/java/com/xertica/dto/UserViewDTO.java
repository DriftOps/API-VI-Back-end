package com.xertica.dto;

import com.xertica.entity.enums.UserRole;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserViewDTO {
    private Long id;
    private String name;
    private String email;
    private UserRole role;
}