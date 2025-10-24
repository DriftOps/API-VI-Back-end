package com.xertica.dto.context;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIContextDTO {
    private UserContextDTO user;
    private AnamnesisContextDTO anamnesis;
}