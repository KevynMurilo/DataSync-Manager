package br.gov.formosa.backup.user.domain.service;

import br.gov.formosa.backup.shared.exception.GlobalBackupException;
import br.gov.formosa.backup.user.domain.model.User;
import br.gov.formosa.backup.user.infra.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
    }

    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() instanceof String) {
            throw new GlobalBackupException("Nenhum usuário autenticado. Por favor, faça login.", HttpStatus.UNAUTHORIZED);
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof User) {
            return (User) principal;
        } else {
            throw new GlobalBackupException("Principal de autenticação inválido.", HttpStatus.UNAUTHORIZED);
        }
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional
    public void deleteUser(UUID id, User authenticatedUser) {
        if (authenticatedUser.getId().equals(id)) {
            throw new GlobalBackupException("Você não pode excluir a si mesmo.", HttpStatus.BAD_REQUEST);
        }
        if (!userRepository.existsById(id)) {
            throw new GlobalBackupException("Usuário não encontrado.", HttpStatus.NOT_FOUND);
        }
        userRepository.deleteById(id);
    }
}