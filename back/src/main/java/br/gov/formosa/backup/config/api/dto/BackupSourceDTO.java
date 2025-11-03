package br.gov.formosa.backup.config.api.dto;

import br.gov.formosa.backup.shared.enums.DatabaseType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BackupSourceDTO(
        @NotBlank String name,
        @NotNull DatabaseType databaseType,
        @NotBlank String dbDumpToolPath,
        String sourcePath,
        String dbHost,
        Integer dbPort,
        String dbName,
        String dbUser,
        String dbPassword
) {}