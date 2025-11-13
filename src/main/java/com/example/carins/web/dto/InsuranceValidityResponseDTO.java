package com.example.carins.web.dto;

public record InsuranceValidityResponseDTO(Long carId,
                                           String date,
                                           boolean valid) {
}