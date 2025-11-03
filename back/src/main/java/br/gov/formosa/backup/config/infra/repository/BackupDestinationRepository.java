package br.gov.formosa.backup.config.infra.repository;

import br.gov.formosa.backup.config.domain.model.BackupDestination;
import br.gov.formosa.backup.user.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BackupDestinationRepository extends JpaRepository<BackupDestination, UUID> {
    List<BackupDestination> findByUser(User user);
    Optional<BackupDestination> findByIdAndUser(UUID id, User user);
    boolean existsByIdAndUser(UUID id, User user);
    void deleteByIdAndUser(UUID id, User user);

    long countByUser(User user);
}