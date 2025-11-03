package br.gov.formosa.backup.dashboard.api.dto;

public record StorageUsageSummaryDTO(
        String sourceName,
        long totalBytes
) {}