package br.gov.formosa.backup.shared.infra.service;

import br.gov.formosa.backup.shared.dto.TestConnectionDTO;
import br.gov.formosa.backup.shared.exception.GlobalBackupException;
import br.gov.formosa.backup.config.domain.model.BackupDestination;
import br.gov.formosa.backup.job.domain.model.BackupRecord;
import br.gov.formosa.backup.config.domain.model.BackupSource;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import lombok.RequiredArgsConstructor;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StorageManagerService {

    private final String TEMP_DIR = System.getProperty("java.io.tmpdir") + File.separator + "backup-temp" + File.separator + UUID.randomUUID();

    public void testConnection(TestConnectionDTO dto) {
        try {
            switch (dto.type()) {
                case GOOGLE_CLOUD_STORAGE:
                    AmazonS3 gcsClient = buildGCSClient(dto.accessKey(), dto.secretKey());
                    gcsClient.doesBucketExistV2(dto.endpoint());
                    break;
                case AMAZON_S3:
                    AmazonS3 s3Client = buildS3Client(dto.accessKey(), dto.secretKey(), dto.region());
                    s3Client.doesBucketExistV2(dto.endpoint());
                    break;
                case LOCAL_DISK:
                    File localDir = new File(dto.endpoint());
                    if (!localDir.exists()) {
                        if (!localDir.mkdirs()) {
                            throw new IOException("Falha ao criar diretório local: " + dto.endpoint());
                        }
                    }
                    if (!localDir.canWrite()) {
                        throw new IOException("Sem permissão de escrita no diretório: " + dto.endpoint());
                    }
                    localDir.delete();
                    break;
                case FTP:
                    FTPClient ftpClient = buildFtpClient(dto.endpoint(), dto.accessKey(), dto.secretKey());
                    ftpClient.logout();
                    ftpClient.disconnect();
                    break;
                default:
                    throw new GlobalBackupException("Tipo de destino não suportado para teste: " + dto.type(), HttpStatus.NOT_IMPLEMENTED);
            }
        } catch (AmazonServiceException e) {
            throw new GlobalBackupException("Falha na conexão Cloud: " + e.getErrorMessage(), HttpStatus.BAD_REQUEST, e);
        } catch (Exception e) {
            throw new GlobalBackupException("Falha na conexão: " + e.getMessage(), HttpStatus.BAD_REQUEST, e);
        }
    }

    public String uploadFile(File fileToUpload, BackupDestination destination, BackupSource source) throws IOException {
        if (!fileToUpload.exists() || fileToUpload.length() == 0) {
            throw new GlobalBackupException("Arquivo de dump inválido ou vazio para upload.", HttpStatus.BAD_REQUEST);
        }

        String remotePath = generateRemotePath(source, fileToUpload.getName());

        try {
            switch (destination.getType()) {
                case GOOGLE_CLOUD_STORAGE:
                    return uploadToGCS(fileToUpload, destination, remotePath);
                case AMAZON_S3:
                    return uploadToS3(fileToUpload, destination, remotePath);
                case LOCAL_DISK:
                    return uploadToLocalDisk(fileToUpload, destination, remotePath);
                case FTP:
                    return uploadToFtp(fileToUpload, destination, remotePath);
                default:
                    throw new GlobalBackupException("Tipo de destino de backup não suportado: " + destination.getType(), HttpStatus.NOT_IMPLEMENTED);
            }
        } catch (IOException e) {
            throw new GlobalBackupException("Falha de I/O durante o upload para o destino " + destination.getType() + ": " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, e);
        } catch (AmazonServiceException e) {
            throw new GlobalBackupException("Falha no serviço Cloud: " + e.getErrorMessage(), HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }

    public File downloadFile(BackupRecord record, BackupDestination destination) {
        File tempDir = new File(TEMP_DIR);
        tempDir.mkdirs();
        File tempFile = new File(tempDir, record.getFilename());

        try {
            switch (destination.getType()) {
                case LOCAL_DISK:
                    downloadFromLocalDisk(record, destination, tempFile);
                    break;
                case GOOGLE_CLOUD_STORAGE:
                    downloadFromGCS(record, destination, tempFile);
                    break;
                case AMAZON_S3:
                    downloadFromS3(record, destination, tempFile);
                    break;
                case FTP:
                    downloadFromFtp(record, destination, tempFile);
                    break;
                default:
                    throw new GlobalBackupException("Tipo de destino de backup não suportado para download.", HttpStatus.NOT_IMPLEMENTED);
            }
        } catch (GlobalBackupException e) {
            throw e;
        } catch (IOException e) {
            throw new GlobalBackupException("Falha de I/O durante o download do destino " + destination.getType() + ": " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, e);
        } catch (AmazonServiceException e) {
            throw new GlobalBackupException("Falha no serviço Cloud: " + e.getErrorMessage(), HttpStatus.INTERNAL_SERVER_ERROR, e);
        }

        if (!tempFile.exists() || (record.getSizeBytes() != null && tempFile.length() != record.getSizeBytes())) {
            throw new GlobalBackupException("Falha no download ou o arquivo temporário está corrompido/vazio.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return tempFile;
    }

    public void deleteFile(BackupRecord record, BackupDestination destination) {
        try {
            switch (destination.getType()) {
                case LOCAL_DISK:
                    deleteFromLocalDisk(record, destination);
                    break;
                case GOOGLE_CLOUD_STORAGE:
                    deleteFromGCS(record, destination);
                    break;
                case AMAZON_S3:
                    deleteFromS3(record, destination);
                    break;
                case FTP:
                    deleteFromFtp(record, destination);
                    break;
                default:
                    throw new GlobalBackupException("Tipo de destino de backup não suportado para exclusão.", HttpStatus.NOT_IMPLEMENTED);
            }
        } catch (IOException e) {
            throw new GlobalBackupException("Falha de I/O durante a exclusão do arquivo no destino " + destination.getType() + ": " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, e);
        } catch (AmazonServiceException e) {
            throw new GlobalBackupException("Falha no serviço Cloud: " + e.getErrorMessage(), HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }

    private String generateRemotePath(BackupSource source, String originalFilename) {
        String sourceName = source.getName().replaceAll("[^a-zA-Z0-9.-]", "_");
        String dateFolder = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        return sourceName + "/" + dateFolder + "/" + originalFilename;
    }

    private AmazonS3 buildS3Client(String accessKey, String secretKey, String region) {
        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        return AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(region)
                .build();
    }

    private AmazonS3 buildGCSClient(String accessKey, String secretKey) {
        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        EndpointConfiguration endpointConfig = new EndpointConfiguration("https://storage.googleapis.com", "auto");
        ClientConfiguration clientConfig = new ClientConfiguration();
        clientConfig.setSignerOverride("AWSS3V4SignerType");

        return AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withEndpointConfiguration(endpointConfig)
                .withClientConfiguration(clientConfig)
                .build();
    }

    private FTPClient buildFtpClient(String endpoint, String accessKey, String secretKey) throws IOException {
        FTPClient ftpClient = new FTPClient();
        ftpClient.connect(endpoint, 21);
        int reply = ftpClient.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            ftpClient.disconnect();
            throw new IOException("Falha ao conectar ao FTP: " + endpoint);
        }
        boolean loggedIn = ftpClient.login(accessKey, secretKey);
        if (!loggedIn) {
            throw new IOException("Falha no login FTP (usuário/senha).");
        }
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        ftpClient.enterLocalPassiveMode();
        return ftpClient;
    }

    private void ensureFtpDirectoryExists(FTPClient ftpClient, String remotePath) throws IOException {
        String[] directories = remotePath.split("/");
        if (directories.length > 1) {
            String[] dirsToCreate = java.util.Arrays.copyOf(directories, directories.length - 1);
            for (String dir : dirsToCreate) {
                if (!dir.isEmpty()) {
                    boolean dirExists = ftpClient.changeWorkingDirectory(dir);
                    if (!dirExists) {
                        if (!ftpClient.makeDirectory(dir)) {
                            throw new IOException("Falha ao criar diretório FTP: " + dir);
                        }
                        if (!ftpClient.changeWorkingDirectory(dir)) {
                            throw new IOException("Falha ao entrar no diretório FTP: " + dir);
                        }
                    }
                }
            }
            ftpClient.changeToParentDirectory();
        }
    }

    private String uploadToLocalDisk(File fileToUpload, BackupDestination destination, String remotePath) throws IOException {
        File baseDir = new File(destination.getEndpoint());
        File destinationFile = new File(baseDir, remotePath);
        destinationFile.getParentFile().mkdirs();
        Path sourcePath = fileToUpload.toPath();
        Path destPath = destinationFile.toPath();
        Files.copy(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING);
        return destPath.toString();
    }

    private void downloadFromLocalDisk(BackupRecord record, BackupDestination destination, File tempFile) throws IOException {
        File sourceFile = new File(record.getRemotePath());
        if (!sourceFile.isAbsolute()) {
            sourceFile = new File(destination.getEndpoint(), record.getRemotePath());
        }
        if (!sourceFile.exists()) {
            throw new GlobalBackupException("Arquivo não encontrado no disco local: " + sourceFile.getAbsolutePath(), HttpStatus.NOT_FOUND);
        }
        Files.copy(sourceFile.toPath(), tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    private void deleteFromLocalDisk(BackupRecord record, BackupDestination destination) {
        File fileToDelete = new File(record.getRemotePath());
        if (!fileToDelete.isAbsolute()) {
            fileToDelete = new File(destination.getEndpoint(), record.getRemotePath());
        }
        fileToDelete.delete();
    }

    private String uploadToS3(File file, BackupDestination dest, String remotePath) throws IOException {
        AmazonS3 s3Client = buildS3Client(dest.getAccessKey(), dest.getSecretKey(), dest.getRegion());
        s3Client.putObject(dest.getEndpoint(), remotePath, file);
        return remotePath;
    }

    private void downloadFromS3(BackupRecord record, BackupDestination dest, File tempFile) throws IOException {
        AmazonS3 s3Client = buildS3Client(dest.getAccessKey(), dest.getSecretKey(), dest.getRegion());
        try (S3Object s3Object = s3Client.getObject(dest.getEndpoint(), record.getRemotePath());
             InputStream objectData = s3Object.getObjectContent()) {
            Files.copy(objectData, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void deleteFromS3(BackupRecord record, BackupDestination dest) throws IOException {
        AmazonS3 s3Client = buildS3Client(dest.getAccessKey(), dest.getSecretKey(), dest.getRegion());
        s3Client.deleteObject(dest.getEndpoint(), record.getRemotePath());
    }

    private String uploadToGCS(File file, BackupDestination dest, String remotePath) throws IOException {
        AmazonS3 gcsClient = buildGCSClient(dest.getAccessKey(), dest.getSecretKey());
        gcsClient.putObject(dest.getEndpoint(), remotePath, file);
        return remotePath;
    }

    private void downloadFromGCS(BackupRecord record, BackupDestination dest, File tempFile) throws IOException {
        AmazonS3 gcsClient = buildGCSClient(dest.getAccessKey(), dest.getSecretKey());
        try (S3Object s3Object = gcsClient.getObject(dest.getEndpoint(), record.getRemotePath());
             InputStream objectData = s3Object.getObjectContent()) {
            Files.copy(objectData, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void deleteFromGCS(BackupRecord record, BackupDestination dest) throws IOException {
        AmazonS3 gcsClient = buildGCSClient(dest.getAccessKey(), dest.getSecretKey());
        gcsClient.deleteObject(dest.getEndpoint(), record.getRemotePath());
    }

    private String uploadToFtp(File file, BackupDestination dest, String remotePath) throws IOException {
        FTPClient ftpClient = null;
        try (InputStream fileStream = new FileInputStream(file)) {
            ftpClient = buildFtpClient(dest.getEndpoint(), dest.getAccessKey(), dest.getSecretKey());
            ensureFtpDirectoryExists(ftpClient, remotePath);
            boolean done = ftpClient.storeFile(remotePath, fileStream);
            if (!done) {
                throw new IOException("Falha ao salvar arquivo no FTP. Resposta: " + ftpClient.getReplyString());
            }
            return remotePath;
        } finally {
            if (ftpClient != null && ftpClient.isConnected()) {
                ftpClient.logout();
                ftpClient.disconnect();
            }
        }
    }

    private void downloadFromFtp(BackupRecord record, BackupDestination dest, File tempFile) throws IOException {
        FTPClient ftpClient = null;
        try (OutputStream outputStream = new FileOutputStream(tempFile)) {
            ftpClient = buildFtpClient(dest.getEndpoint(), dest.getAccessKey(), dest.getSecretKey());
            boolean success = ftpClient.retrieveFile(record.getRemotePath(), outputStream);
            if (!success) {
                throw new IOException("Falha ao baixar arquivo do FTP. Resposta: " + ftpClient.getReplyString());
            }
        } finally {
            if (ftpClient != null && ftpClient.isConnected()) {
                ftpClient.logout();
                ftpClient.disconnect();
            }
        }
    }

    private void deleteFromFtp(BackupRecord record, BackupDestination dest) throws IOException {
        FTPClient ftpClient = null;
        try {
            ftpClient = buildFtpClient(dest.getEndpoint(), dest.getAccessKey(), dest.getSecretKey());
            boolean deleted = ftpClient.deleteFile(record.getRemotePath());
            if (!deleted) {
                throw new IOException("Falha ao excluir arquivo do FTP. Resposta: " + ftpClient.getReplyString());
            }
        } finally {
            if (ftpClient != null && ftpClient.isConnected()) {
                ftpClient.logout();
                ftpClient.disconnect();
            }
        }
    }
}