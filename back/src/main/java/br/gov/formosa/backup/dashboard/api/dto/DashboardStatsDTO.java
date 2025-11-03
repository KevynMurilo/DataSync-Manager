package br.gov.formosa.backup.dashboard.api.dto;

public record DashboardStatsDTO(
        long totalJobs,
        long totalSources,
        long totalDestinations,
        long totalStorageUsedBytes,
        double successRatePercentage
) {}