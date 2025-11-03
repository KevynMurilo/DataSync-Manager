package br.gov.formosa.backup.job.api.dto;

public record BackupExecutionDTO(
        String customDestinationId,
        boolean ignoreSchedule
) {
}