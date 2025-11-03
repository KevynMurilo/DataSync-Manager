package br.gov.formosa.backup.config.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EmailConfigDTO(
        @NotBlank String name,
        @NotBlank String host,
        @NotNull Integer port,
        @NotBlank @Email String username,
        String password
) {}