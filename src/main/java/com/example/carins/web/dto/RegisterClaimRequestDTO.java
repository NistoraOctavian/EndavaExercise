package com.example.carins.web.dto;

import java.time.LocalDate;

public record RegisterClaimRequestDTO(LocalDate claimDate,
                                      String description,
                                      int amount) {
}