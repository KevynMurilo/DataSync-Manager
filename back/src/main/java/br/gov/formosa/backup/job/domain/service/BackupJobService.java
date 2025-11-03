package br.gov.formosa.backup.job.domain.service;

import br.gov.formosa.backup.job.api.dto.BackupJobDTO;
import br.gov.formosa.backup.shared.exception.GlobalBackupException;
import br.gov.formosa.backup.job.api.mapper.BackupJobMapper;
import br.gov.formosa.backup.job.domain.model.BackupJob;
import br.gov.formosa.backup.user.domain.model.User;
import br.gov.formosa.backup.job.infra.repository.BackupJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BackupJobService {

    private final BackupJobRepository repository;
    private final BackupJobMapper mapper;

    @Transactional
    public BackupJob save(BackupJobDTO dto, User user) {
        BackupJob job = mapper.toEntity(dto);
        job.setUser(user);
        return repository.save(job);
    }

    @Transactional
    public BackupJob update(UUID id, BackupJobDTO dto, User user) {
        if (!repository.existsByIdAndUser(id, user)) {
            throw new GlobalBackupException("Job de backup não encontrado: " + id, HttpStatus.NOT_FOUND);
        }
        BackupJob job = mapper.toEntity(dto);
        job.setId(id);
        job.setUser(user);
        return repository.save(job);
    }

    public BackupJob findById(UUID id, User user) {
        return repository.findByIdAndUser(id, user)
                .orElseThrow(() -> new GlobalBackupException("Job de backup não encontrado: " + id, HttpStatus.NOT_FOUND));
    }

    public BackupJob findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new GlobalBackupException("Job de backup não encontrado: " + id, HttpStatus.NOT_FOUND));
    }

    public List<BackupJob> findAll(User user) {
        return repository.findByUser(user);
    }

    public List<BackupJob> findAllForScheduler() {
        return repository.findAll();
    }

    public List<BackupJob> findAllActiveJobsByUser(User user) {
        return repository.findAllActiveScheduledJobsByUser(user);
    }

    public List<BackupJob> findAllActiveJobsForScheduler() {
        return repository.findAllActiveScheduledJobs();
    }

    @Transactional
    public void deleteById(UUID id, User user) {
        if (!repository.existsByIdAndUser(id, user)) {
            throw new GlobalBackupException("Job de backup não encontrado: " + id, HttpStatus.NOT_FOUND);
        }
        repository.deleteByIdAndUser(id, user);
    }
}