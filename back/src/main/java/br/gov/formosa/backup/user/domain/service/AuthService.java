package br.gov.formosa.backup.user.domain.service;

import br.gov.formosa.backup.user.api.dto.ChangePasswordDTO;
import br.gov.formosa.backup.user.api.dto.JwtResponseDTO;
import br.gov.formosa.backup.user.api.dto.LoginRequestDTO;
import br.gov.formosa.backup.user.api.dto.RegisterRequestDTO;
import br.gov.formosa.backup.shared.exception.GlobalBackupException;
import br.gov.formosa.backup.user.domain.model.User;
import br.gov.formosa.backup.shared.security.JwtService;
import br.gov.formosa.backup.user.infra.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public User createUser(RegisterRequestDTO request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new GlobalBackupException("Este e-mail já está em uso.", HttpStatus.BAD_REQUEST);
        }
        var user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .mustChangePassword(true)
                .build();
        return userRepository.save(user);
    }

    public JwtResponseDTO login(LoginRequestDTO request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        var user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Email ou senha inválidos."));
        var jwt = jwtService.generateToken(user);
        return new JwtResponseDTO(jwt, user.isMustChangePassword());
    }

    @Transactional
    public JwtResponseDTO changePassword(User user, ChangePasswordDTO request) {
        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            throw new BadCredentialsException("Senha antiga inválida.");
        }

        if (!user.getEmail().equals(request.email()) && userRepository.findByEmail(request.email()).isPresent()) {
            throw new GlobalBackupException("O novo e-mail informado já está em uso.", HttpStatus.BAD_REQUEST);
        }

        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setMustChangePassword(false);
        User savedUser = userRepository.save(user);

        var jwt = jwtService.generateToken(savedUser);
        return new JwtResponseDTO(jwt, false);
    }
}