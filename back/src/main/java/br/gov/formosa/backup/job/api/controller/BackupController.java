package br.gov.formosa.backup.job.api.controller;

import br.gov.formosa.backup.job.domain.model.BackupRecord;
import br.gov.formosa.backup.user.domain.model.User;
import br.gov.formosa.backup.job.domain.service.BackupService;
import br.gov.formosa.backup.user.domain.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/backup")
@RequiredArgsConstructor
public class BackupController {

    private final BackupService backupService;
    private final UserService userService;

    @GetMapping("/history")
    public Page<BackupRecord> getBackupHistory(@PageableDefault(size = 10, sort = "timestamp,desc") Pageable pageable) {
        User user = userService.getAuthenticatedUser();
        return backupService.getHistory(user, pageable);
    }

    @PostMapping("/restore/{recordId}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void restoreBackup(@PathVariable UUID recordId) {
        User user = userService.getAuthenticatedUser();
        backupService.restoreBackup(recordId, user);
    }
}