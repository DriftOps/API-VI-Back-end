package com.xertica.dto;

import java.time.LocalDate;

public record WeightLogDTO(
    Double weight, 
    LocalDate logDate) {}
