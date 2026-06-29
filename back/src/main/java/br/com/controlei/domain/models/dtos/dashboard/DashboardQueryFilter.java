package br.com.controlei.domain.models.dtos.dashboard;

import java.time.LocalDate;

public record DashboardQueryFilter(
        LocalDate startDate,
        LocalDate endDate
) {
}
