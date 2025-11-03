package br.gov.formosa.backup.shared.exception;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error; // Nome do Status HTTP (e.g., BAD_REQUEST, INTERNAL_SERVER_ERROR)
    private String message;
    private String details; // Mensagem da exceção
}