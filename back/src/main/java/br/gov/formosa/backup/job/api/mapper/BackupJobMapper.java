package br.gov.formosa.backup.job.api.mapper;

import br.gov.formosa.backup.job.api.dto.BackupJobDTO;
import br.gov.formosa.backup.config.domain.model.BackupDestination;
import br.gov.formosa.backup.job.domain.model.BackupJob;
import br.gov.formosa.backup.config.domain.model.BackupSource;
import br.gov.formosa.backup.config.domain.model.EmailConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public abstract class BackupJobMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "source", source = "sourceId")
    @Mapping(target = "destinations", source = "destinationIds")
    @Mapping(target = "emailConfig", source = "emailConfigId")
    public abstract BackupJob toEntity(BackupJobDTO dto);

    protected BackupSource mapSource(UUID sourceId) {
        if (sourceId == null) return null;
        BackupSource source = new BackupSource();
        source.setId(sourceId);
        return source;
    }

    protected Set<BackupDestination> mapDestinations(Set<UUID> destinationIds) {
        if (destinationIds == null) return null;
        return destinationIds.stream().map(id -> {
            BackupDestination dest = new BackupDestination();
            dest.setId(id);
            return dest;
        }).collect(Collectors.toSet());
    }

    protected EmailConfig mapEmailConfig(UUID emailConfigId) {
        if (emailConfigId == null) return null;
        EmailConfig emailConfig = new EmailConfig();
        emailConfig.setId(emailConfigId);
        return emailConfig;
    }
}