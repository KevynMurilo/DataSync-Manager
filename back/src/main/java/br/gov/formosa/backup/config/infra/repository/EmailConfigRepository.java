package br.gov.formosa.backup.config.infra.repository;

import br.gov.formosa.backup.config.domain.model.EmailConfig;
import br.gov.formosa.backup.user.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmailConfigRepository extends JpaRepository<EmailConfig, UUID> {
    List<EmailConfig> findByUser(User user);
    Optional<EmailConfig> findByIdAndUser(UUID id, User user);
    boolean existsByIdAndUser(UUID id, User user);
    void deleteByIdAndUser(UUID id, User user);
}