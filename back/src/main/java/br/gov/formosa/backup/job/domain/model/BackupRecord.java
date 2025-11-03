package br.gov.formosa.backup.job.domain.model;

import br.gov.formosa.backup.shared.enums.BackupStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "backup_record")
public class BackupRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "job_id")
    private BackupJob job;

    @Column(name = "filename", length = 255)
    private String filename;

    // O caminho/chave completo no storage (ex: "Banco_Principal/2025-11-01/backup.sql")
    @Column(name = "remote_path", length = 512)
    private String remotePath;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    @Column(name = "destination_id")
    private UUID destinationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private BackupStatus status;

    @Column(name = "log_summary", columnDefinition = "TEXT")
    private String logSummary;
}