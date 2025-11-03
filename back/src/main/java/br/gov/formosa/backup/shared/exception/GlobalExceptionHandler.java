package br.gov.formosa.backup.shared.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(GlobalBackupException.class)
    public ResponseEntity<ErrorResponse> handleGlobalBackupException(
            GlobalBackupException ex, WebRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(ex.getStatus().value())
                .error(ex.getStatus().getReasonPhrase())
                .message("Ocorreu uma falha no serviço de backup/restauração.")
                .details(ex.getMessage())
                .build();

        return new ResponseEntity<>(errorResponse, ex.getStatus());
    }
}