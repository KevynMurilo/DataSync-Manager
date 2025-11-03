package br.gov.formosa.backup.dashboard.api.dto;

import br.gov.formosa.backup.job.domain.model.BackupJob;
import br.gov.formosa.backup.job.domain.model.BackupRecord;

import java.util.List;

public record DashboardDTO(
        DashboardStatsDTO stats,
        List<BackupStatusSummaryDTO> dailyStatusSummary,
        List<StorageUsageSummaryDTO> storageBySource,
        List<BackupJob> upcomingJobs,
        List<BackupRecord> recentFailures
) {}