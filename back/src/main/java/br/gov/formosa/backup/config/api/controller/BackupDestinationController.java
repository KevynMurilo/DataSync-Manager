package br.gov.formosa.backup.config.api.controller;

import br.gov.formosa.backup.config.api.dto.BackupDestinationDTO;
import br.gov.formosa.backup.shared.dto.TestConnectionDTO;
import br.gov.formosa.backup.config.domain.model.BackupDestination;
import br.gov.formosa.backup.user.domain.model.User;
import br.gov.formosa.backup.config.domain.service.BackupDestinationService;
import br.gov.formosa.backup.shared.infra.service.StorageManagerService;
import br.gov.formosa.backup.user.domain.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/backup-destination")
@RequiredArgsConstructor
public class BackupDestinationController {

    private final BackupDestinationService backupDestinationService;
    private final StorageManagerService storageManagerService;
    private final UserService userService;

    @GetMapping
    public List<BackupDestination> findAll() {
        User user = userService.getAuthenticatedUser();
        return backupDestinationService.findAll(user);
    }

    @GetMapping("/{id}")
    public BackupDestination findOne(@PathVariable String id) {
        User user = userService.getAuthenticatedUser();
        return backupDestinationService.findById(id, user);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BackupDestination save(@Valid @RequestBody BackupDestinationDTO dto) {
        User user = userService.getAuthenticatedUser();
        return backupDestinationService.save(dto, user);
    }

    @PutMapping("/{id}")
    public BackupDestination update(@PathVariable String id, @Valid @RequestBody BackupDestinationDTO dto) {
        User user = userService.getAuthenticatedUser();
        return backupDestinationService.update(id, dto, user);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        User user = userService.getAuthenticatedUser();
        backupDestinationService.deleteById(id, user);
    }

    @PostMapping("/test")
    public ResponseEntity<?> testConnection(@Valid @RequestBody TestConnectionDTO dto) {
        try {
            storageManagerService.testConnection(dto);
            return ResponseEntity.ok(Map.of("message", "Conexão bem-sucedida!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Falha na conexão: " + e.getMessage()));
        }
    }
}