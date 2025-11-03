package br.gov.formosa.backup.config.domain.service;

import br.gov.formosa.backup.config.api.dto.BackupDestinationDTO;
import br.gov.formosa.backup.shared.exception.GlobalBackupException;
import br.gov.formosa.backup.config.api.mapper.BackupDestinationMapper;
import br.gov.formosa.backup.config.domain.model.BackupDestination;
import br.gov.formosa.backup.user.domain.model.User;
import br.gov.formosa.backup.shared.enums.BackupType;
import br.gov.formosa.backup.config.infra.repository.BackupDestinationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BackupDestinationService {

    private final BackupDestinationRepository repository;
    private final BackupDestinationMapper mapper;

    @Transactional
    public BackupDestination save(BackupDestinationDTO dto, User user) {
        validateDestinationCredentials(dto);
        BackupDestination entity = mapper.toEntity(dto);
        entity.setUser(user);
        return repository.save(entity);
    }

    @Transactional
    public BackupDestination update(String id, BackupDestinationDTO dto, User user) {
        UUID uuid = parseUUID(id);

        repository.findByIdAndUser(uuid, user)
                .orElseThrow(() -> new GlobalBackupException("Destino de backup não encontrado com ID: " + id, HttpStatus.NOT_FOUND));

        validateDestinationCredentials(dto);

        BackupDestination entity = mapper.toEntity(dto);
        entity.setId(uuid);
        entity.setUser(user);
        return repository.save(entity);
    }

    public BackupDestination findById(String id, User user) {
        UUID uuid = parseUUID(id);
        return repository.findByIdAndUser(uuid, user)
                .orElseThrow(() -> new GlobalBackupException("Destino de backup não encontrado com ID: " + id, HttpStatus.NOT_FOUND));
    }

    public List<BackupDestination> findAll(User user) {
        return repository.findByUser(user);
    }

    @Transactional
    public void deleteById(String id, User user) {
        UUID uuid = parseUUID(id);
        if (!repository.existsByIdAndUser(uuid, user)) {
            throw new GlobalBackupException("Destino de backup não encontrado com ID: " + id, HttpStatus.NOT_FOUND);
        }
        repository.deleteByIdAndUser(uuid, user);
    }

    private UUID parseUUID(String id) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new GlobalBackupException("ID de destino de backup inválido: " + id, HttpStatus.BAD_REQUEST);
        }
    }

    private void validateDestinationCredentials(BackupDestinationDTO dto) {
        if (dto.type() != BackupType.LOCAL_DISK) {
            if (dto.accessKey() == null || dto.accessKey().isBlank() || dto.secretKey() == null || dto.secretKey().isBlank()) {
                throw new GlobalBackupException("Destinos de Cloud/FTP requerem Access Key e Secret Key.", HttpStatus.BAD_REQUEST);
            }
        }
    }
}