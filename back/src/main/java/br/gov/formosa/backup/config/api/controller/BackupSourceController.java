package br.gov.formosa.backup.config.api.controller;

import br.gov.formosa.backup.config.api.dto.BackupSourceDTO;
import br.gov.formosa.backup.config.domain.model.BackupSource;
import br.gov.formosa.backup.user.domain.model.User;
import br.gov.formosa.backup.config.domain.service.BackupSourceService;
import br.gov.formosa.backup.user.domain.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/backup-sources")
@RequiredArgsConstructor
public class BackupSourceController {

    private final BackupSourceService sourceService;
    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BackupSource create(@Valid @RequestBody BackupSourceDTO dto) {
        User user = userService.getAuthenticatedUser();
        return sourceService.save(dto, user);
    }

    @GetMapping
    public List<BackupSource> findAll() {
        User user = userService.getAuthenticatedUser();
        return sourceService.findAll(user);
    }

    @GetMapping("/{id}")
    public BackupSource findById(@PathVariable UUID id) {
        User user = userService.getAuthenticatedUser();
        return sourceService.findById(id, user);
    }

    @PutMapping("/{id}")
    public BackupSource update(@PathVariable UUID id, @Valid @RequestBody BackupSourceDTO dto) {
        User user = userService.getAuthenticatedUser();
        return sourceService.update(id, dto, user);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        User user = userService.getAuthenticatedUser();
        sourceService.deleteById(id, user);
    }

    @PostMapping("/test")
    public ResponseEntity<?> testConnection(@Valid @RequestBody BackupSourceDTO dto) {
        try {
            sourceService.testConnection(dto);
            return ResponseEntity.ok(Map.of("message", "Conexão bem-sucedida!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Falha na conexão: " + e.getMessage()));
        }
    }
}