package br.gov.formosa.backup.user.api.dto;

public record JwtResponseDTO(
        String token,
        boolean mustChangePassword
) {}