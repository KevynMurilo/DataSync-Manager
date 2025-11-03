package br.gov.formosa.backup.dashboard.api.dto;

import br.gov.formosa.backup.shared.enums.BackupStatus;
import java.time.LocalDate;

public record DailyStatusQueryResult(
        LocalDate date,
        BackupStatus status,
        long count
) {}