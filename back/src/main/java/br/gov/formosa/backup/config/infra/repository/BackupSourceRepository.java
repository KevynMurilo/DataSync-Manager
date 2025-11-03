package br.gov.formosa.backup.config.infra.repository;

import br.gov.formosa.backup.config.domain.model.BackupSource;
import br.gov.formosa.backup.user.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BackupSourceRepository extends JpaRepository<BackupSource, UUID> {
    List<BackupSource> findByUser(User user);
    Optional<BackupSource> findByIdAndUser(UUID id, User user);
    boolean existsByIdAndUser(UUID id, User user);
    void deleteByIdAndUser(UUID id, User user);

    long countByUser(User user);
}