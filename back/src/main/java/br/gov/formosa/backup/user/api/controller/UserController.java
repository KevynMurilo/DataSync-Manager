package br.gov.formosa.backup.user.api.controller;

import br.gov.formosa.backup.user.api.dto.RegisterRequestDTO;
import br.gov.formosa.backup.user.domain.model.User;
import br.gov.formosa.backup.user.domain.service.AuthService;
import br.gov.formosa.backup.user.domain.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    @GetMapping
    public List<User> getAllUsers() {
        return userService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User createUser(@Valid @RequestBody RegisterRequestDTO request) {
        return authService.createUser(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable UUID id) {
        User authenticatedUser = userService.getAuthenticatedUser();
        userService.deleteUser(id, authenticatedUser);
    }
}