package com.xertica.dto;

public record FaqViewDTO(
    Long id,
    String question,
    String answer,
    String tags
) {}