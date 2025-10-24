package com.xertica.dto.context;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserContextDTO {
    private String name;
    private Double weight;
    private Double height;
    private Integer age;
    private String gender;
}