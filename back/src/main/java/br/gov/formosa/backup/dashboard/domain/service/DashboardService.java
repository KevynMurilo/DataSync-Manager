package br.gov.formosa.backup.dashboard.domain.service;

import br.gov.formosa.backup.dashboard.api.dto.DashboardDTO;
import br.gov.formosa.backup.dashboard.api.dto.DashboardStatsDTO;
import br.gov.formosa.backup.dashboard.api.dto.BackupStatusSummaryDTO;
import br.gov.formosa.backup.dashboard.api.dto.DailyStatusQueryResult;
import br.gov.formosa.backup.dashboard.api.dto.StorageUsageQueryResult;
import br.gov.formosa.backup.dashboard.api.dto.StorageUsageSummaryDTO;
import br.gov.formosa.backup.job.domain.model.BackupJob;
import br.gov.formosa.backup.job.domain.model.BackupRecord;
import br.gov.formosa.backup.user.domain.model.User;
import br.gov.formosa.backup.shared.enums.BackupStatus;
import br.gov.formosa.backup.config.infra.repository.BackupDestinationRepository;
import br.gov.formosa.backup.job.infra.repository.BackupJobRepository;
import br.gov.formosa.backup.job.infra.repository.BackupRecordRepository;
import br.gov.formosa.backup.config.infra.repository.BackupSourceRepository;
import br.gov.formosa.backup.user.domain.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final BackupRecordRepository recordRepository;
    private final BackupJobRepository jobRepository;
    private final BackupSourceRepository sourceRepository;
    private final BackupDestinationRepository destinationRepository;
    private final UserService userService;

    public DashboardDTO getDashboardData() {
        User user = userService.getAuthenticatedUser();

        DashboardStatsDTO stats = getDashboardStats(user);
        List<BackupStatusSummaryDTO> dailySummary = getDailyStatusSummary(user);
        List<StorageUsageSummaryDTO> storageSummary = getStorageBySource(user);
        List<BackupJob> upcomingJobs = getUpcomingJobs(user);
        List<BackupRecord> recentFailures = getRecentFailures(user);

        return new DashboardDTO(stats, dailySummary, storageSummary, upcomingJobs, recentFailures);
    }

    private DashboardStatsDTO getDashboardStats(User user) {
        long totalJobs = jobRepository.countByUser(user);
        long totalSources = sourceRepository.countByUser(user);
        long totalDestinations = destinationRepository.countByUser(user);

        Long totalStorage = recordRepository.sumTotalSizeBytesByUser(user);
        long totalStorageUsed = (totalStorage != null) ? totalStorage : 0L;

        long successCount = recordRepository.countByStatusAndJobUser(BackupStatus.SUCCESS, user);
        long failedCount = recordRepository.countByStatusAndJobUser(BackupStatus.FAILED, user);
        long totalCompleted = successCount + failedCount;

        double successRate = (totalCompleted == 0) ? 100.0 : (successCount * 100.0) / totalCompleted;

        return new DashboardStatsDTO(totalJobs, totalSources, totalDestinations, totalStorageUsed, successRate);
    }

    private List<BackupStatusSummaryDTO> getDailyStatusSummary(User user) {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7).with(LocalTime.MIN);
        List<DailyStatusQueryResult> queryResults = recordRepository.findDailyStatusSummaryByUser(sevenDaysAgo, user);

        Map<LocalDate, Map<BackupStatus, Long>> groupedData = queryResults.stream()
                .collect(Collectors.groupingBy(
                        DailyStatusQueryResult::date,
                        Collectors.toMap(DailyStatusQueryResult::status, DailyStatusQueryResult::count)
                ));

        return groupedData.entrySet().stream()
                .map(entry -> new BackupStatusSummaryDTO(
                        entry.getKey(),
                        entry.getValue().getOrDefault(BackupStatus.SUCCESS, 0L),
                        entry.getValue().getOrDefault(BackupStatus.FAILED, 0L)
                ))
                .sorted(Comparator.comparing(BackupStatusSummaryDTO::date))
                .collect(Collectors.toList());
    }

    private List<StorageUsageSummaryDTO> getStorageBySource(User user) {
        List<StorageUsageQueryResult> queryResults = recordRepository.findStorageUsageBySourceByUser(user);

        return queryResults.stream()
                .map(r -> new StorageUsageSummaryDTO(r.sourceName(), r.totalBytes()))
                .collect(Collectors.toList());
    }

    private List<BackupJob> getUpcomingJobs(User user) {
        LocalTime now = LocalTime.now();

        return jobRepository.findAllActiveScheduledJobsByUser(user).stream()
                .sorted(Comparator.comparing(BackupJob::getBackupTime))
                .filter(job -> job.getBackupTime().isAfter(now))
                .limit(5)
                .collect(Collectors.toList());
    }

    private List<BackupRecord> getRecentFailures(User user) {
        return recordRepository.findTop5ByStatusAndJobUserOrderByTimestampDesc(BackupStatus.FAILED, user, PageRequest.of(0, 5));
    }
}