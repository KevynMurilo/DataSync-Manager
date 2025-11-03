package br.gov.formosa.backup.job.infra.repository;

import br.gov.formosa.backup.dashboard.api.dto.DailyStatusQueryResult;
import br.gov.formosa.backup.dashboard.api.dto.StorageUsageQueryResult;
import br.gov.formosa.backup.job.domain.model.BackupJob;
import br.gov.formosa.backup.job.domain.model.BackupRecord;
import br.gov.formosa.backup.user.domain.model.User;
import br.gov.formosa.backup.shared.enums.BackupStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BackupRecordRepository extends JpaRepository<BackupRecord, UUID> {

    @Query("SELECT r FROM BackupRecord r WHERE r.job.user = :user ORDER BY r.timestamp DESC")
    Page<BackupRecord> findAllByUserOrderByTimestampDesc(User user, Pageable pageable);

    List<BackupRecord> findByJobAndTimestampBeforeAndStatus(
            BackupJob job,
            LocalDateTime cutOffDate,
            BackupStatus status
    );

    @Query("SELECT r FROM BackupRecord r WHERE r.id = :id AND r.job.user = :user")
    Optional<BackupRecord> findByIdAndUser(UUID id, User user);

    @Query("SELECT SUM(r.sizeBytes) FROM BackupRecord r WHERE r.status = 'SUCCESS' AND r.job.user = :user")
    Long sumTotalSizeBytesByUser(User user);

    long countByStatusAndJobUser(BackupStatus status, User user);

    @Query("SELECT r FROM BackupRecord r WHERE r.status = :status AND r.job.user = :user ORDER BY r.timestamp DESC")
    List<BackupRecord> findTop5ByStatusAndJobUserOrderByTimestampDesc(BackupStatus status, User user, Pageable pageable);

    @Query("SELECT new br.gov.formosa.backup.dashboard.api.dto.DailyStatusQueryResult(" +
            "CAST(r.timestamp AS java.time.LocalDate) as date, r.status as status, COUNT(r) as count) " +
            "FROM BackupRecord r WHERE r.timestamp >= :since AND r.job.user = :user " +
            "GROUP BY date, r.status ORDER BY date ASC")
    List<DailyStatusQueryResult> findDailyStatusSummaryByUser(@Param("since") LocalDateTime since, @Param("user") User user);

    @Query("SELECT new br.gov.formosa.backup.dashboard.api.dto.StorageUsageQueryResult(" +
            "r.job.source.name as sourceName, SUM(r.sizeBytes) as totalBytes) " +
            "FROM BackupRecord r " +
            "WHERE r.status = 'SUCCESS' AND r.job.user = :user " +
            "GROUP BY r.job.source.name ORDER BY totalBytes DESC")
    List<StorageUsageQueryResult> findStorageUsageBySourceByUser(User user);
}