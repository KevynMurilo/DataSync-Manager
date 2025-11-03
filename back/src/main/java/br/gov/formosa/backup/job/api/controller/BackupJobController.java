package br.gov.formosa.backup.job.api.controller;

import br.gov.formosa.backup.job.api.dto.BackupJobDTO;
import br.gov.formosa.backup.job.domain.model.BackupJob;
import br.gov.formosa.backup.user.domain.model.User;
import br.gov.formosa.backup.job.domain.service.BackupJobService;
import br.gov.formosa.backup.job.domain.service.BackupService;
import br.gov.formosa.backup.user.domain.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/backup-jobs")
@RequiredArgsConstructor
public class BackupJobController {

    private final BackupJobService jobService;
    private final BackupService backupService;
    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BackupJob create(@Valid @RequestBody BackupJobDTO dto) {
        User user = userService.getAuthenticatedUser();
        return jobService.save(dto, user);
    }

    @GetMapping
    public List<BackupJob> findAll() {
        User user = userService.getAuthenticatedUser();
        return jobService.findAll(user);
    }

    @GetMapping("/{id}")
    public BackupJob findById(@PathVariable UUID id) {
        User user = userService.getAuthenticatedUser();
        return jobService.findById(id, user);
    }

    @PutMapping("/{id}")
    public BackupJob update(@PathVariable UUID id, @Valid @RequestBody BackupJobDTO dto) {
        User user = userService.getAuthenticatedUser();
        return jobService.update(id, dto, user);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        User user = userService.getAuthenticatedUser();
        jobService.deleteById(id, user);
    }

    @PostMapping("/{id}/execute")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void triggerManualJob(@PathVariable UUID id) {
        User user = userService.getAuthenticatedUser();
        backupService.executeJob(id, user);
    }
}