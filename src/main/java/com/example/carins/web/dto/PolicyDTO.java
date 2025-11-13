package com.example.carins.web.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record PolicyDTO(long carId,
                        String provider,
                        LocalDate startDate,
                        @NotNull LocalDate endDate) {
}
