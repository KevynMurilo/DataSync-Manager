package br.gov.formosa.backup.job.infra.repository;

import br.gov.formosa.backup.job.domain.model.BackupJob;
import br.gov.formosa.backup.user.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BackupJobRepository extends JpaRepository<BackupJob, UUID> {

    List<BackupJob> findByUser(User user);
    Optional<BackupJob> findByIdAndUser(UUID id, User user);
    boolean existsByIdAndUser(UUID id, User user);
    void deleteByIdAndUser(UUID id, User user);

    @Query("SELECT j FROM BackupJob j WHERE j.isActive = true AND j.scheduleType <> 'MANUAL' AND j.user = :user")
    List<BackupJob> findAllActiveScheduledJobsByUser(User user);

    @Query("SELECT j FROM BackupJob j WHERE j.isActive = true AND j.scheduleType <> 'MANUAL'")
    List<BackupJob> findAllActiveScheduledJobs();

    long countByUser(User user);
}