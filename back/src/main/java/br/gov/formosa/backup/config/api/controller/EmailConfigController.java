package br.gov.formosa.backup.config.api.controller;

import br.gov.formosa.backup.config.api.dto.EmailConfigDTO;
import br.gov.formosa.backup.config.domain.model.EmailConfig;
import br.gov.formosa.backup.user.domain.model.User;
import br.gov.formosa.backup.config.domain.service.EmailConfigService;
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
@RequestMapping("/api/email-configs")
@RequiredArgsConstructor
public class EmailConfigController {

    private final EmailConfigService emailConfigService;
    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmailConfig create(@Valid @RequestBody EmailConfigDTO dto) {
        User user = userService.getAuthenticatedUser();
        return emailConfigService.save(dto, user);
    }

    @GetMapping
    public List<EmailConfig> findAll() {
        User user = userService.getAuthenticatedUser();
        return emailConfigService.findAll(user);
    }

    @GetMapping("/{id}")
    public EmailConfig findById(@PathVariable UUID id) {
        User user = userService.getAuthenticatedUser();
        return emailConfigService.findById(id, user);
    }

    @PutMapping("/{id}")
    public EmailConfig update(@PathVariable UUID id, @Valid @RequestBody EmailConfigDTO dto) {
        User user = userService.getAuthenticatedUser();
        return emailConfigService.update(id, dto, user);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        User user = userService.getAuthenticatedUser();
        emailConfigService.deleteById(id, user);
    }

    @PostMapping("/test")
    public ResponseEntity<?> testConnection(@Valid @RequestBody EmailConfigDTO dto) {
        try {
            emailConfigService.testConnection(dto);
            return ResponseEntity.ok(Map.of("message", "Conexão bem-sucedida! E-mail de teste enviado para " + dto.username()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Falha na conexão: " + e.getMessage()));
        }
    }
}