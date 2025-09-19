package com.xertica.dto;

import jakarta.validation.constraints.NotBlank;

public record FaqCreateDTO(
    @NotBlank String question,
    @NotBlank String answer,
    String tags
) {}
