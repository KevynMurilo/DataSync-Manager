package br.gov.formosa.backup.shared.dto;

import br.gov.formosa.backup.shared.enums.BackupType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TestConnectionDTO(
        @NotBlank String name,
        @NotNull BackupType type,
        @NotBlank String endpoint,
        String region,
        String accessKey,
        String secretKey,
        @NotNull Boolean isActive
) {
}