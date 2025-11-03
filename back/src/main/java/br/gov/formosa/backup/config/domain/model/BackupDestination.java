package br.gov.formosa.backup.config.domain.model;

import br.gov.formosa.backup.user.domain.model.User;
import br.gov.formosa.backup.shared.enums.BackupType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "backup_destination")
public class BackupDestination {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private BackupType type;

    @Column(name = "endpoint", nullable = false, length = 255)
    private String endpoint;

    @Column(name = "region", length = 50)
    private String region;

    @JsonIgnore
    @Column(name = "access_key", length = 100)
    private String accessKey;

    @JsonIgnore
    @Column(name = "secret_key", length = 100)
    private String secretKey;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}