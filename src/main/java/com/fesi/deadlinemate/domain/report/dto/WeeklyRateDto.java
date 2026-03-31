package com.fesi.deadlinemate.domain.report.dto;

import java.math.BigDecimal;

public record WeeklyRateDto(
        int week,
        BigDecimal rate
) {
}
