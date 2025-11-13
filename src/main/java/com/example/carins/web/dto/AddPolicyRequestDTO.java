package com.example.carins.web.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record AddPolicyRequestDTO(String provider,
                                  @NotNull LocalDate startDate,
                                  @NotNull LocalDate endDate) {
}