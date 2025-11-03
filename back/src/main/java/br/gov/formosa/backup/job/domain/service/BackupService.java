package br.gov.formosa.backup.job.domain.service;

import br.gov.formosa.backup.shared.exception.GlobalBackupException;
import br.gov.formosa.backup.config.domain.model.BackupDestination;
import br.gov.formosa.backup.job.domain.model.BackupJob;
import br.gov.formosa.backup.job.domain.model.BackupRecord;
import br.gov.formosa.backup.config.domain.model.BackupSource;
import br.gov.formosa.backup.user.domain.model.User;
import br.gov.formosa.backup.shared.enums.BackupStatus;
import br.gov.formosa.backup.job.infra.repository.BackupRecordRepository;
import br.gov.formosa.backup.config.domain.service.BackupDestinationService;
import br.gov.formosa.backup.shared.infra.service.NotificationService;
import br.gov.formosa.backup.shared.infra.service.StorageManagerService;
import br.gov.formosa.backup.user.domain.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BackupService {

    private final BackupJobService jobService;
    private final BackupRecordRepository recordRepository;
    private final StorageManagerService storageManager;
    private final BackupDestinationService destinationService;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserService userService;
    private final String TEMP_DIR = System.getProperty("java.io.tmpdir");

    @Transactional
    public void executeJob(UUID jobId, User user) {
        BackupJob job = jobService.findById(jobId, user);
        executeJobInternal(job);
    }

    @Transactional
    public void executeJob(UUID jobId) {
        BackupJob job = jobService.findById(jobId);
        executeJobInternal(job);
    }

    private void executeJobInternal(BackupJob job) {
        if (job.getDestinations() == null || job.getDestinations().isEmpty()) {
            throw new GlobalBackupException("O Job " + job.getName() + " não possui destinos configurados.", HttpStatus.BAD_REQUEST);
        }

        BackupSource source = job.getSource();
        File dumpFile = null;

        try {
            dumpFile = executeDatabaseDump(source, job.getId());

            for (BackupDestination destination : job.getDestinations()) {
                BackupRecord record = createRecord(job, destination, dumpFile);
                try {
                    String remotePath = storageManager.uploadFile(dumpFile, destination, source);

                    record.setRemotePath(remotePath);
                    record.setLogSummary("Backup finalizado com sucesso. Caminho: " + remotePath);
                    record.setStatus(BackupStatus.SUCCESS);

                } catch (Exception e) {
                    record.setLogSummary("FALHA CRÍTICA NO UPLOAD: " + e.getMessage());
                    record.setStatus(BackupStatus.FAILED);
                }
                recordRepository.save(record);
                notificationService.sendBackupNotification(job, record);
            }

        } catch (Exception e) {
            BackupRecord failRecord = createRecord(job, null, null);
            failRecord.setLogSummary("FALHA CRÍTICA NO DUMP: " + e.getMessage());
            failRecord.setStatus(BackupStatus.FAILED);
            recordRepository.save(failRecord);
            notificationService.sendBackupNotification(job, failRecord);

            throw new GlobalBackupException("Falha ao executar dump para o Job: " + job.getName(), HttpStatus.INTERNAL_SERVER_ERROR, e);

        } finally {
            if (dumpFile != null && dumpFile.exists()) {
                dumpFile.delete();
            }
        }
    }

    private BackupRecord createRecord(BackupJob job, BackupDestination destination, File file) {
        BackupRecord record = BackupRecord.builder()
                .job(job)
                .timestamp(LocalDateTime.now())
                .destinationId(destination != null ? destination.getId() : null)
                .status(BackupStatus.IN_PROGRESS)
                .build();
        if (file != null) {
            record.setFilename(file.getName());
            record.setSizeBytes(file.length());
        }
        return recordRepository.save(record);
    }

    private File executeDatabaseDump(BackupSource source, UUID jobId) throws Exception {
        String baseName = source.getName().replaceAll("[^a-zA-Z0-9.-]", "_");
        String filename = baseName + "_" + LocalDateTime.now().toString().replaceAll(":", "-") + ".sql";
        File outputFile = new File(TEMP_DIR, filename);

        String topic = "/topic/logs/job/" + jobId;

        List<String> command = new ArrayList<>();
        Map<String, String> environment = new java.util.HashMap<>();

        buildDumpCommand(source, command, environment);

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.environment().putAll(environment);
        pb.redirectOutput(outputFile);

        messagingTemplate.convertAndSend(topic, "INICIANDO PROCESSO DE DUMP...");
        messagingTemplate.convertAndSend(topic, "Comando: " + String.join(" ", command));

        Process process = pb.start();

        new Thread(() -> new BufferedReader(new InputStreamReader(process.getInputStream())).lines()
                .forEach(line -> messagingTemplate.convertAndSend(topic, line))).start();

        new Thread(() -> new BufferedReader(new InputStreamReader(process.getErrorStream())).lines()
                .forEach(line -> messagingTemplate.convertAndSend(topic, "[ERRO] " + line))).start();

        int exitCode = process.waitFor();
        messagingTemplate.convertAndSend(topic, "PROCESSO FINALIZADO (Código de Saída: " + exitCode + ")");

        if (exitCode != 0) {
            outputFile.delete();
            throw new GlobalBackupException(
                    "Dump falhou. Código de saída: " + exitCode + ". Verifique o log para detalhes.",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }

        if (!outputFile.exists() || outputFile.length() == 0) {
            throw new GlobalBackupException(
                    "Dump executado, mas o arquivo de saída está vazio.",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }

        messagingTemplate.convertAndSend(topic, "Dump concluído com sucesso. Arquivo: " + outputFile.getName());
        return outputFile;
    }

    private void buildDumpCommand(BackupSource source, List<String> command, Map<String, String> environment) {
        String host = source.getDbHost();
        Integer port = source.getDbPort();

        command.add(source.getDbDumpToolPath());

        try {
            switch (source.getDatabaseType()) {
                case POSTGRES:
                    buildPostgresDumpCommand(source, command, environment, host, port);
                    break;
                case MYSQL:
                case MARIADB:
                    buildMySqlMariaDbDumpCommand(source, command, environment, host, port);
                    break;
                case ORACLE:
                    buildOracleDumpCommand(source, command, environment);
                    break;
                case SQLSERVER:
                    buildSqlServerDumpCommand(source, command, environment, host, port);
                    break;
                case MONGODB:
                    buildMongoDbDumpCommand(source, command, environment, host, port);
                    break;
                case H2:
                    buildH2DumpCommand(source, command, environment, host, port);
                    break;
                default:
                    throw new GlobalBackupException(
                            "Tipo de banco de dados não suportado para dump: " + source.getDatabaseType(),
                            HttpStatus.BAD_REQUEST
                    );
            }
        } catch (UnsupportedOperationException e) {
            throw new GlobalBackupException(e.getMessage(), HttpStatus.NOT_IMPLEMENTED, e);
        }
    }

    private void buildPostgresDumpCommand(BackupSource source, List<String> command, Map<String, String> environment, String host, Integer port) {
        environment.put("PGPASSWORD", source.getDbPassword());
        command.addAll(Arrays.asList(
                "-h", host,
                "-U", source.getDbUser(),
                source.getDbName()
        ));
        if (port != null && port > 0) {
            command.add("-p");
            command.add(String.valueOf(port));
        }
    }

    private void buildMySqlMariaDbDumpCommand(BackupSource source, List<String> command, Map<String, String> environment, String host, Integer port) {
        environment.put("MYSQL_PWD", source.getDbPassword());
        command.addAll(Arrays.asList(
                "-h", host,
                "-u", source.getDbUser(),
                source.getDbName()
        ));
        if (port != null && port > 0) {
            command.add("--port");
            command.add(String.valueOf(port));
        }
    }

    private void buildOracleDumpCommand(BackupSource source, List<String> command, Map<String, String> environment) {
        String schema = source.getDbUser();
        String connectionString = source.getDbUser() + "/" + source.getDbPassword() + "@" + source.getDbHost();
        if (source.getDbPort() != null && source.getDbPort() > 0) {
            connectionString += ":" + source.getDbPort();
        }
        if (source.getDbName() != null && !source.getDbName().isBlank()) {
            connectionString += "/" + source.getDbName();
        }

        command.addAll(Arrays.asList(
                connectionString,
                "DUMPFILE=backup_oracle.dmp",
                "DIRECTORY=DATA_PUMP_DIR",
                "SCHEMAS=" + schema,
                "LOGFILE=expdp_" + schema + ".log"
        ));

        throw new UnsupportedOperationException("Implementação do Oracle Data Pump (expdp) requer lógica de cópia do arquivo do diretório Oracle.");
    }

    private void buildSqlServerDumpCommand(BackupSource source, List<String> command, Map<String, String> environment, String host, Integer port) {
        String connection = "-S " + host + (port != null && port > 0 ? "," + port : "") + " -U " + source.getDbUser() + " -P " + source.getDbPassword();

        command.addAll(Arrays.asList(
                connection,
                "-d", source.getDbName(),
                "-Q", "BACKUP DATABASE [" + source.getDbName() + "] TO DISK = N'" + TEMP_DIR + File.separator + "sqlserver_backup.bak' WITH NOFORMAT, NOINIT, NAME = N'Full Backup', SKIP, NOREWIND, NOUNLOAD, STATS = 10"
        ));

        throw new UnsupportedOperationException("Implementação do SQL Server (sqlcmd) requer lógica de cópia do arquivo .bak do servidor.");
    }

    private void buildMongoDbDumpCommand(BackupSource source, List<String> command, Map<String, String> environment, String host, Integer port) {
        command.addAll(Arrays.asList(
                "--host", host,
                "--port", (port != null && port > 0) ? String.valueOf(port) : "27017",
                "--db", source.getDbName(),
                "--username", source.getDbUser(),
                "--password", source.getDbPassword(),
                "--archive"
        ));
    }

    private void buildH2DumpCommand(BackupSource source, List<String> command, Map<String, String> environment, String host, Integer port) {
        throw new UnsupportedOperationException("O Backup H2 deve ser feito via JDBC (BACKUP TO 'file.zip') e não via ProcessBuilder com comandos de shell.");
    }

    public Page<BackupRecord> getHistory(User user, Pageable pageable) {
        return recordRepository.findAllByUserOrderByTimestampDesc(user, pageable);
    }

    @Transactional
    public void restoreBackup(UUID recordId, User user) {
        BackupRecord record = recordRepository.findByIdAndUser(recordId, user)
                .orElseThrow(() -> new GlobalBackupException("Registro de backup não encontrado ou não pertence a você.", HttpStatus.NOT_FOUND));

        if (record.getStatus() != BackupStatus.SUCCESS) {
            throw new GlobalBackupException(
                    "A restauração só pode ser feita com um backup de status SUCESSO. Status atual: " + record.getStatus(),
                    HttpStatus.CONFLICT
            );
        }

        BackupSource source = record.getJob().getSource();
        BackupDestination destination = destinationService.findById(record.getDestinationId().toString(), user);

        File downloadedFile = null;
        try {
            downloadedFile = storageManager.downloadFile(record, destination);
            executeDatabaseRestore(source, downloadedFile);
        } catch (GlobalBackupException e) {
            throw e;
        } catch (Exception e) {
            throw new GlobalBackupException("Falha ao restaurar backup: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, e);
        } finally {
            if (downloadedFile != null && downloadedFile.exists()) {
                downloadedFile.delete();
            }
        }
    }

    private void executeDatabaseRestore(BackupSource source, File dumpFile) throws Exception {
        List<String> command = new ArrayList<>();
        Map<String, String> environment = new java.util.HashMap<>();

        String restoreTool = buildRestoreCommand(source, command, environment);

        command.add(0, restoreTool);

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.environment().putAll(environment);
        pb.redirectInput(dumpFile);

        Process process = pb.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            String errorLog = readProcessError(process);
            throw new GlobalBackupException(
                    "Restauração do banco de dados falhou. Log: " + errorLog,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    private String buildRestoreCommand(BackupSource source, List<String> command, Map<String, String> environment) {
        String host = source.getDbHost();
        Integer port = source.getDbPort();

        String dumpTool = source.getDbDumpToolPath().toLowerCase();
        String restoreTool;

        try {
            switch (source.getDatabaseType()) {
                case POSTGRES:
                    restoreTool = dumpTool.replace("pg_dump", "psql");
                    buildPostgresRestoreCommand(source, command, environment, host, port);
                    return restoreTool;

                case MYSQL:
                case MARIADB:
                    restoreTool = dumpTool.replace("mysqldump", "mysql").replace("mariadb-dump", "mariadb");
                    buildMySqlMariaDbRestoreCommand(source, command, environment, host, port);
                    return restoreTool;

                case ORACLE:
                    restoreTool = dumpTool.replace("expdp", "impdp");
                    buildOracleRestoreCommand(source, command, environment);
                    return restoreTool;

                case SQLSERVER:
                    restoreTool = dumpTool.replace("bcp", "sqlcmd");
                    buildSqlServerRestoreCommand(source, command, environment, host, port);
                    return restoreTool;

                case MONGODB:
                    restoreTool = dumpTool.replace("mongodump", "mongorestore");
                    buildMongoDbRestoreCommand(source, command, environment, host, port);
                    return restoreTool;

                case H2:
                    restoreTool = "java";
                    buildH2RestoreCommand(source, command, environment, host, port);
                    return restoreTool;

                default:
                    throw new GlobalBackupException(
                            "Tipo de banco de dados não suportado para restore: " + source.getDatabaseType(),
                            HttpStatus.BAD_REQUEST
                    );
            }
        } catch (UnsupportedOperationException e) {
            throw new GlobalBackupException(e.getMessage(), HttpStatus.NOT_IMPLEMENTED, e);
        }
    }

    private void buildPostgresRestoreCommand(BackupSource source, List<String> command, Map<String, String> environment, String host, Integer port) {
        environment.put("PGPASSWORD", source.getDbPassword());
        command.addAll(Arrays.asList(
                "-h", host,
                "-U", source.getDbUser(),
                "-d", source.getDbName()
        ));
        if (port != null && port > 0) {
            command.add("-p");
            command.add(String.valueOf(port));
        }
    }

    private void buildMySqlMariaDbRestoreCommand(BackupSource source, List<String> command, Map<String, String> environment, String host, Integer port) {
        environment.put("MYSQL_PWD", source.getDbPassword());
        command.addAll(Arrays.asList(
                "-h", host,
                "-u", source.getDbUser(),
                source.getDbName()
        ));
        if (port != null && port > 0) {
            command.add("--port");
            command.add(String.valueOf(port));
        }
    }

    private void buildOracleRestoreCommand(BackupSource source, List<String> command, Map<String, String> environment) {
        String schema = source.getDbUser();
        String connectionString = source.getDbUser() + "/" + source.getDbPassword() + "@" + source.getDbHost();
        if (source.getDbPort() != null && source.getDbPort() > 0) {
            connectionString += ":" + source.getDbPort();
        }
        if (source.getDbName() != null && !source.getDbName().isBlank()) {
            connectionString += "/" + source.getDbName();
        }

        command.addAll(Arrays.asList(
                connectionString,
                "DUMPFILE=backup_oracle.dmp",
                "DIRECTORY=DATA_PUMP_DIR",
                "SCHEMAS=" + schema,
                "LOGFILE=impdp_" + schema + ".log",
                "REMAP_SCHEMA=" + schema + ":" + schema
        ));

        throw new UnsupportedOperationException("Implementação do Oracle Data Pump (impdp) requer lógica de mover o arquivo de restore para o diretório Oracle.");
    }

    private void buildSqlServerRestoreCommand(BackupSource source, List<String> command, Map<String, String> environment, String host, Integer port) {
        String connection = "-S " + host + (port != null && port > 0 ? "," + port : "") + " -U " + source.getDbUser() + " -P " + source.getDbPassword();

        command.addAll(Arrays.asList(
                connection,
                "-Q", "RESTORE DATABASE [" + source.getDbName() + "] FROM DISK = N'" + TEMP_DIR + File.separator + "sqlserver_backup.bak' WITH FILE = 1, NOUNLOAD, REPLACE, STATS = 5"
        ));

        throw new UnsupportedOperationException("Implementação do SQL Server (sqlcmd) requer lógica de mover o arquivo .bak para o servidor e adaptar a string de comando.");
    }

    private void buildMongoDbRestoreCommand(BackupSource source, List<String> command, Map<String, String> environment, String host, Integer port) {
        command.addAll(Arrays.asList(
                "--host", host,
                "--port", (port != null && port > 0) ? String.valueOf(port) : "27017",
                "--db", source.getDbName(),
                "--username", source.getDbUser(),
                "--password", source.getDbPassword(),
                "--drop",
                "--archive"
        ));
    }

    private void buildH2RestoreCommand(BackupSource source, List<String> command, Map<String, String> environment, String host, Integer port) {
        throw new UnsupportedOperationException("O Restore H2 deve ser feito via JDBC (RunScript/SCRIPT FROM) e não via ProcessBuilder com comandos de shell.");
    }

    private String readProcessError(Process process) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        StringBuilder errorLog = new StringBuilder();
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                errorLog.append(line).append("\n");
            }
        } catch (Exception e) {
            errorLog.append("Erro ao ler log de erro: ").append(e.getMessage());
        }
        return errorLog.toString();
    }
}