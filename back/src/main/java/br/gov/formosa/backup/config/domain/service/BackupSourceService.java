package br.gov.formosa.backup.config.domain.service;

import br.gov.formosa.backup.config.api.dto.BackupSourceDTO;
import br.gov.formosa.backup.shared.exception.GlobalBackupException;
import br.gov.formosa.backup.config.api.mapper.BackupSourceMapper;
import br.gov.formosa.backup.config.domain.model.BackupSource;
import br.gov.formosa.backup.user.domain.model.User;
import br.gov.formosa.backup.config.infra.repository.BackupSourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class BackupSourceService {

    private final BackupSourceRepository repository;
    private final BackupSourceMapper mapper;

    @Transactional
    public BackupSource save(BackupSourceDTO dto, User user) {
        BackupSource source = mapper.toEntity(dto);
        source.setUser(user);
        return repository.save(source);
    }

    @Transactional
    public BackupSource update(UUID id, BackupSourceDTO dto, User user) {
        if (!repository.existsByIdAndUser(id, user)) {
            throw new GlobalBackupException("Fonte de backup (Source) não encontrada: " + id, HttpStatus.NOT_FOUND);
        }
        BackupSource source = mapper.toEntity(dto);
        source.setId(id);
        source.setUser(user);
        return repository.save(source);
    }

    public BackupSource findById(UUID id, User user) {
        return repository.findByIdAndUser(id, user)
                .orElseThrow(() -> new GlobalBackupException("Fonte de backup (Source) não encontrada: " + id, HttpStatus.NOT_FOUND));
    }

    public List<BackupSource> findAll(User user) {
        return repository.findByUser(user);
    }

    @Transactional
    public void deleteById(UUID id, User user) {
        if (!repository.existsByIdAndUser(id, user)) {
            throw new GlobalBackupException("Fonte de backup (Source) não encontrada: " + id, HttpStatus.NOT_FOUND);
        }
        repository.deleteById(id);
    }

    public void testConnection(BackupSourceDTO dto) {
        try {
            switch (dto.databaseType()) {
                case POSTGRES:
                    testPostgresConnection(dto);
                    break;
                case MYSQL:
                case MARIADB:
                    testMySqlConnection(dto);
                    break;
                default:
                    throw new UnsupportedOperationException("Teste de conexão não implementado para " + dto.databaseType());
            }
        } catch (IOException | InterruptedException e) {
            throw new GlobalBackupException("Falha ao executar teste: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }

    private void testPostgresConnection(BackupSourceDTO dto) throws IOException, InterruptedException {
        String pgIsReadyPath = dto.dbDumpToolPath().toLowerCase().replace("pg_dump", "pg_isready");
        List<String> command = new ArrayList<>();
        command.add(pgIsReadyPath);
        command.add("-h");
        command.add(dto.dbHost());
        command.add("-U");
        command.add(dto.dbUser());
        command.add("-d");
        command.add(dto.dbName());
        if (dto.dbPort() != null && dto.dbPort() > 0) {
            command.add("-p");
            command.add(String.valueOf(dto.dbPort()));
        }

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.environment().put("PGPASSWORD", dto.dbPassword());

        executeTestProcess(pb);
    }

    private void testMySqlConnection(BackupSourceDTO dto) throws IOException, InterruptedException {
        String mysqlAdminPath = dto.dbDumpToolPath().toLowerCase().replace("mysqldump", "mysqladmin").replace("mariadb-dump", "mysqladmin");
        List<String> command = new ArrayList<>();
        command.add(mysqlAdminPath);
        command.add("-h");
        command.add(dto.dbHost());
        command.add("-u");
        command.add(dto.dbUser());

        if (dto.dbPort() != null && dto.dbPort() > 0) {
            command.add("-P");
            command.add(String.valueOf(dto.dbPort()));
        }
        command.add("ping");

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.environment().put("MYSQL_PWD", dto.dbPassword());

        executeTestProcess(pb);
    }

    private void executeTestProcess(ProcessBuilder pb) throws IOException, InterruptedException {
        Process process = pb.start();
        boolean finished = process.waitFor(10, TimeUnit.SECONDS);

        if (!finished) {
            process.destroy();
            throw new IOException("Tempo limite (10s) atingido. Verifique o host e a porta.");
        }

        if (process.exitValue() != 0) {
            String error = readProcessError(process);
            throw new IOException("Falha no teste: " + error);
        }
    }

    private String readProcessError(Process process) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        StringBuilder errorLog = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            errorLog.append(line);
        }
        return errorLog.toString();
    }
}