package br.gov.formosa.backup.job.api.dto;

import br.gov.formosa.backup.shared.enums.NotificationPolicy;
import br.gov.formosa.backup.shared.enums.ScheduleType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;

public record BackupJobDTO(
        @NotBlank String name,
        @NotNull UUID sourceId,
        @NotEmpty Set<UUID> destinationIds,
        @NotNull ScheduleType scheduleType,
        @NotNull LocalTime backupTime,
        @Min(1) int retentionDays,
        @NotNull Boolean isActive,

        @NotNull NotificationPolicy notificationPolicy,

        String notificationRecipients,

        UUID emailConfigId
) {}