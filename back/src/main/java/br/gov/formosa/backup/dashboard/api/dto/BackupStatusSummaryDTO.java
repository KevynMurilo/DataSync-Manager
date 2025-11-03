package br.gov.formosa.backup.dashboard.api.dto;

import java.time.LocalDate;

public record BackupStatusSummaryDTO(
        LocalDate date,
        long successCount,
        long failedCount
) {}