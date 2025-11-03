package br.gov.formosa.backup.config.domain.service;

import br.gov.formosa.backup.config.api.dto.EmailConfigDTO;
import br.gov.formosa.backup.shared.exception.GlobalBackupException;
import br.gov.formosa.backup.config.domain.model.EmailConfig;
import br.gov.formosa.backup.user.domain.model.User;
import br.gov.formosa.backup.config.infra.repository.EmailConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Properties;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailConfigService {

    private final EmailConfigRepository repository;

    @Transactional
    public EmailConfig save(EmailConfigDTO dto, User user) {
        EmailConfig config = new EmailConfig();
        config.setName(dto.name());
        config.setHost(dto.host());
        config.setPort(dto.port());
        config.setUsername(dto.username());
        config.setPassword(dto.password());
        config.setUser(user);

        return repository.save(config);
    }

    @Transactional
    public EmailConfig update(UUID id, EmailConfigDTO dto, User user) {
        EmailConfig config = findById(id, user);
        config.setName(dto.name());
        config.setHost(dto.host());
        config.setPort(dto.port());
        config.setUsername(dto.username());

        if (dto.password() != null && !dto.password().isBlank()) {
            config.setPassword(dto.password());
        }

        return repository.save(config);
    }

    public EmailConfig findById(UUID id, User user) {
        return repository.findByIdAndUser(id, user)
                .orElseThrow(() -> new GlobalBackupException("Configuração de E-mail não encontrada: " + id, HttpStatus.NOT_FOUND));
    }

    public EmailConfig findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new GlobalBackupException("Configuração de E-mail não encontrada: " + id, HttpStatus.NOT_FOUND));
    }

    public List<EmailConfig> findAll(User user) {
        return repository.findByUser(user);
    }

    @Transactional
    public void deleteById(UUID id, User user) {
        if (!repository.existsByIdAndUser(id, user)) {
            throw new GlobalBackupException("Configuração de E-mail não encontrada: " + id, HttpStatus.NOT_FOUND);
        }
        repository.deleteByIdAndUser(id, user);
    }

    public void testConnection(EmailConfigDTO dto) {
        JavaMailSenderImpl mailSender = createDynamicMailSender(dto);

        try {
            mailSender.testConnection();

            SimpleMailMessage testMessage = new SimpleMailMessage();
            testMessage.setFrom(dto.username());
            testMessage.setTo(dto.username());
            testMessage.setSubject("Teste de Conexão - CoreVault Backup System");
            testMessage.setText("Sua configuração de e-mail SMTP foi validada com sucesso!");

            mailSender.send(testMessage);
        } catch (Exception e) {
            throw new GlobalBackupException("Falha ao conectar ou enviar e-mail: " + e.getMessage(), HttpStatus.BAD_REQUEST, e);
        }
    }

    private JavaMailSenderImpl createDynamicMailSender(EmailConfigDTO config) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(config.host());
        mailSender.setPort(config.port());
        mailSender.setUsername(config.username());
        mailSender.setPassword(config.password());

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.connectiontimeout", "5000");
        props.put("mail.smtp.timeout", "5000");

        return mailSender;
    }
}