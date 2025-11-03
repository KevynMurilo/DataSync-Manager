package br.gov.formosa.backup.job.domain.service;

import br.gov.formosa.backup.shared.exception.GlobalBackupException;
import br.gov.formosa.backup.config.domain.model.BackupDestination;
import br.gov.formosa.backup.job.domain.model.BackupJob;
import br.gov.formosa.backup.job.domain.model.BackupRecord;
import br.gov.formosa.backup.shared.enums.BackupStatus;
import br.gov.formosa.backup.job.infra.repository.BackupRecordRepository;
import br.gov.formosa.backup.config.domain.service.BackupDestinationService;
import br.gov.formosa.backup.shared.infra.service.ErrorLogService;
import br.gov.formosa.backup.shared.infra.service.StorageManagerService;
import br.gov.formosa.backup.user.domain.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SchedulerService {

    private final BackupService backupService;
    private final BackupJobService jobService;
    private final BackupRecordRepository recordRepository;
    private final StorageManagerService storageManager;
    private final BackupDestinationService destinationService;
    private final ErrorLogService errorLogService;
    private final UserService userService;

    @Scheduled(fixedRate = 900000)
    @Transactional
    public void triggerDueJobs() {
        LocalTime now = LocalTime.now();
        LocalDate today = LocalDate.now();
        DayOfWeek todayOfWeek = today.getDayOfWeek();

        List<BackupJob> jobs = jobService.findAllActiveJobsForScheduler();

        for (BackupJob job : jobs) {
            if (isTimeDue(job.getBackupTime(), now)) {

                boolean shouldRun = false;
                switch (job.getScheduleType()) {
                    case DAILY:
                        shouldRun = true;
                        break;
                    case WEEKLY:
                        shouldRun = true;
                        break;
                    case MANUAL:
                        break;
                }

                if (shouldRun) {
                    try {
                        backupService.executeJob(job.getId());
                    } catch (Exception e) {
                        errorLogService.logScheduledError("Job " + job.getName(), e);
                    }
                }
            }
        }
    }

    private boolean isTimeDue(LocalTime jobTime, LocalTime now) {
        return !now.isBefore(jobTime) && now.isBefore(jobTime.plusMinutes(15));
    }

    @Scheduled(fixedRate = 300000)
    @Transactional
    public void executeCleanupTask() {
        try {
            List<BackupJob> jobs = jobService.findAllForScheduler();
            for (BackupJob job : jobs) {
                if (job.isActive()) {
                    cleanupOldBackups(job);
                }
            }
        } catch (Exception e) {
            errorLogService.logScheduledError("Limpeza Agendada Global", e);
        }
    }

    @Transactional
    public void cleanupOldBackups(BackupJob job) {
        int retentionDays = job.getRetentionDays();
        LocalDateTime cutOffDate = LocalDateTime.now().minusDays(retentionDays);

        List<BackupRecord> recordsToDelete = recordRepository.findByJobAndTimestampBeforeAndStatus(job, cutOffDate, BackupStatus.SUCCESS);

        for (BackupRecord record : recordsToDelete) {
            try {
                BackupDestination destination = destinationService.findById(record.getDestinationId().toString(), job.getUser());
                storageManager.deleteFile(record, destination);
                recordRepository.delete(record);
            } catch (GlobalBackupException e) {
                errorLogService.logScheduledError("Limpeza do Backup " + record.getFilename() + " (Job: " + job.getName() + ")", e);
            } catch (Exception e) {
                errorLogService.logScheduledError("Limpeza do Backup " + record.getFilename() + " (Erro Genérico)", e);
            }
        }
    }
}