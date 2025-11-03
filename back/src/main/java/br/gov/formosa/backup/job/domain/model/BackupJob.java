package br.gov.formosa.backup.job.domain.model;

import br.gov.formosa.backup.config.domain.model.BackupDestination;
import br.gov.formosa.backup.config.domain.model.BackupSource;
import br.gov.formosa.backup.config.domain.model.EmailConfig;
import br.gov.formosa.backup.user.domain.model.User;
import br.gov.formosa.backup.shared.enums.NotificationPolicy;
import br.gov.formosa.backup.shared.enums.ScheduleType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;

@Data
@Entity
@Table(name = "backup_job")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class BackupJob {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(optional = false)
    @JoinColumn(name = "source_id", nullable = false)
    private BackupSource source;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "job_destinations",
            joinColumns = @JoinColumn(name = "job_id"),
            inverseJoinColumns = @JoinColumn(name = "destination_id")
    )
    private Set<BackupDestination> destinations;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScheduleType scheduleType = ScheduleType.DAILY;

    @Column(nullable = false)
    private LocalTime backupTime;

    @Column(nullable = false)
    private int retentionDays;

    @Column(nullable = false)
    private boolean isActive;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationPolicy notificationPolicy = NotificationPolicy.NEVER;

    @Column(columnDefinition = "TEXT")
    private String notificationRecipients;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "email_config_id")
    private EmailConfig emailConfig;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}