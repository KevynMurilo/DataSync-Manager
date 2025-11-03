package br.gov.formosa.backup.shared.infra.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ErrorLogService {
    private static final Logger log = LoggerFactory.getLogger(ErrorLogService.class);

    public void logScheduledError(String taskName, Exception e) {
        log.error("Falha na Tarefa Agendada [{}]. Mensagem: {}", taskName, e.getMessage(), e);
    }
}