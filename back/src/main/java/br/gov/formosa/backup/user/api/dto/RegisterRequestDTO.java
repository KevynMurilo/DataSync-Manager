package br.gov.formosa.backup.user.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequestDTO(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6) String password
) {}