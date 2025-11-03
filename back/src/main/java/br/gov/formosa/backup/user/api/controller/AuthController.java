package br.gov.formosa.backup.user.api.controller;

import br.gov.formosa.backup.user.api.dto.ChangePasswordDTO;
import br.gov.formosa.backup.user.api.dto.JwtResponseDTO;
import br.gov.formosa.backup.user.api.dto.LoginRequestDTO;
import br.gov.formosa.backup.user.domain.model.User;
import br.gov.formosa.backup.user.domain.service.AuthService;
import br.gov.formosa.backup.user.domain.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/login")
    public JwtResponseDTO login(@Valid @RequestBody LoginRequestDTO request) {
        return authService.login(request);
    }

    @PostMapping("/change-password")
    public ResponseEntity<JwtResponseDTO> changePassword(@Valid @RequestBody ChangePasswordDTO request) {
        User user = userService.getAuthenticatedUser();
        JwtResponseDTO response = authService.changePassword(user, request);
        return ResponseEntity.ok(response);
    }
}