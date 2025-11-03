package br.gov.formosa.backup.config.api.mapper;

import br.gov.formosa.backup.config.api.dto.BackupSourceDTO;
import br.gov.formosa.backup.config.domain.model.BackupSource;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BackupSourceMapper {
    BackupSource toEntity(BackupSourceDTO dto);
    BackupSourceDTO toDTO(BackupSource entity);
}