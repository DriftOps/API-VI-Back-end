package com.xertica.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginDTO {
    private String email;
    private String password;
}