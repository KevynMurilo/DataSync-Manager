package br.gov.formosa.backup.config.domain.model;

import br.gov.formosa.backup.user.domain.model.User;
import br.gov.formosa.backup.shared.enums.DatabaseType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Data
@Entity
@Table(name = "backup_source")
public class BackupSource {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DatabaseType databaseType;

    @Column(length = 255)
    private String sourcePath;

    @Column(length = 255)
    private String dbHost;

    @Column
    private Integer dbPort;

    @Column(length = 255)
    private String dbName;

    @Column(length = 100)
    private String dbUser;

    @JsonIgnore
    @Column(length = 100)
    private String dbPassword;

    @Column(length = 255, nullable = false)
    private String dbDumpToolPath;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}