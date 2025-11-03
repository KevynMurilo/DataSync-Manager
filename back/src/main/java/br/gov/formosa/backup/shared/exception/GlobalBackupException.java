package br.gov.formosa.backup.shared.exception;

import org.springframework.http.HttpStatus;

public class GlobalBackupException extends RuntimeException {
    private final HttpStatus status;
    private final String details;

    public GlobalBackupException(String message, HttpStatus status) {
        super(message);
        this.status = status;
        this.details = message;
    }

    public GlobalBackupException(String message, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.status = status;
        this.details = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getDetails() {
        return details;
    }
}