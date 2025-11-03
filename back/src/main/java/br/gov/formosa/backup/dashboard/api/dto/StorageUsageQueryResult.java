package br.gov.formosa.backup.dashboard.api.dto;

public record StorageUsageQueryResult(
        String sourceName,
        long totalBytes
) {}