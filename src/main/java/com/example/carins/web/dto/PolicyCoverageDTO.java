package com.example.carins.web.dto;

import java.util.List;

public record PolicyCoverageDTO(List<DateRangeDTO> gaps,
                                List<DateRangeDTO> coverageWindows) {
}
