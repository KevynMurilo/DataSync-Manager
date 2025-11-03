package br.gov.formosa.backup.shared.infra.service;

import br.gov.formosa.backup.config.domain.service.EmailConfigService;
import br.gov.formosa.backup.job.domain.model.BackupJob;
import br.gov.formosa.backup.job.domain.model.BackupRecord;
import br.gov.formosa.backup.config.domain.model.EmailConfig;
import br.gov.formosa.backup.shared.enums.BackupStatus;
import br.gov.formosa.backup.shared.enums.NotificationPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Properties;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final EmailConfigService emailConfigService;

    @Async
    public void sendBackupNotification(BackupJob job, BackupRecord record) {
        if (!shouldSend(job, record.getStatus())) {
            return;
        }

        if (job.getEmailConfig() == null || job.getEmailConfig().getId() == null) {
            System.err.println("Job " + job.getName() + " configurado para notificar, mas nenhum EmailConfig foi associado.");
            return;
        }

        try {
            EmailConfig config = emailConfigService.findById(job.getEmailConfig().getId());

            JavaMailSender mailSender = createDynamicMailSender(config);
            String from = config.getUsername();

            SimpleMailMessage message = createMailMessage(job, record, from);
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Falha ao enviar e-mail de notificação: " + e.getMessage());
        }
    }

    private JavaMailSender createDynamicMailSender(EmailConfig config) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(config.getHost());
        mailSender.setPort(config.getPort());
        mailSender.setUsername(config.getUsername());

        mailSender.setPassword(config.getPassword());

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        return mailSender;
    }

    private boolean shouldSend(BackupJob job, BackupStatus status) {
        if (job.getNotificationPolicy() == NotificationPolicy.NEVER) {
            return false;
        }
        if (job.getNotificationRecipients() == null || job.getNotificationRecipients().isBlank()) {
            return false;
        }
        if (job.getNotificationPolicy() == NotificationPolicy.ALWAYS) {
            return true;
        }
        if (job.getNotificationPolicy() == NotificationPolicy.ON_FAILURE && status == BackupStatus.FAILED) {
            return true;
        }
        return false;
    }

    private String[] getRecipients(BackupJob job) {
        return job.getNotificationRecipients().split("[,;\\s]+");
    }

    private SimpleMailMessage createMailMessage(BackupJob job, BackupRecord record, String from) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(getRecipients(job));

        String statusStr = record.getStatus().toString();
        String subject = String.format("Status do Backup: %s - %s", statusStr, job.getName());

        String body = buildEmailBody(job, record);

        message.setSubject(subject);
        message.setText(body);
        return message;
    }

    private String buildEmailBody(BackupJob job, BackupRecord record) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm:ss");

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Relatório de Execução do Job de Backup: %s\n", job.getName()));
        sb.append("--------------------------------------------------\n");
        sb.append(String.format("Status: %s\n", record.getStatus()));
        sb.append(String.format("Data/Hora: %s\n", record.getTimestamp().format(formatter)));
        sb.append(String.format("Fonte de Dados: %s\n", job.getSource().getName()));

        if (record.getStatus() == BackupStatus.SUCCESS) {
            double sizeInMB = (record.getSizeBytes() != null ? record.getSizeBytes() : 0) / (1024.0 * 1024.0);
            sb.append(String.format("Arquivo: %s\n", record.getFilename()));
            sb.append(String.format("Tamanho: %.2f MB\n", sizeInMB));
            sb.append(String.format("Caminho no Storage: %s\n", record.getRemotePath()));
        } else {
            sb.append("\n--- LOG DE ERRO ---\n");
            sb.append(record.getLogSummary());
        }

        sb.append("\n--------------------------------------------------\n");
        sb.append("CoreVault Backup System");

        return sb.toString();
    }
}