package br.gov.formosa.backup.config.api.mapper;

import br.gov.formosa.backup.config.api.dto.BackupDestinationDTO;
import br.gov.formosa.backup.config.domain.model.BackupDestination;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BackupDestinationMapper {
    BackupDestination toEntity(BackupDestinationDTO dto);
    BackupDestinationDTO toDTO(BackupDestination entity);
}