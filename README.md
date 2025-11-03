## 1\. Entidades do Sistema de Backup (Lombok)

As entidades centrais para um sistema de gestão de backups são: o registro da operação de backup, a configuração de onde salvar e a configuração de quando e o que salvar.

### 1\. `BackupDestination` (Destino de Backup)

Representa onde o arquivo de backup será armazenado (local, S3, Google Cloud, etc.).

```java
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * Representa um local de armazenamento configurado para guardar os backups.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BackupDestination {
    private String id; // ID único, ex: "S3_PROD_BR"
    private String name; // Nome amigável do destino
    private BackupType type; // Tipo de destino (ENUM: LOCAL_DISK, AMAZON_S3, GCS, etc.)
    private String endpoint; // URL/Caminho do bucket/diretório
    private String region; // Região (para serviços de cloud)
    private String accessKey; // Credencial de acesso (cuidado ao armazenar!)
    private boolean isActive; // Se o destino está em uso
}

public enum BackupType {
    LOCAL_DISK,
    AMAZON_S3,
    GOOGLE_CLOUD_STORAGE,
    FTP
}
```

### 2\. `SystemConfiguration` (Configuração do Sistema)

Representa as regras de automação e agendamento.

```java
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * Configurações globais e de agendamento para a rotina de backup.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfiguration {
    private Long id; // Geralmente 1L, pois há apenas uma configuração global
    private String backupCronExpression; // Expressão CRON para agendamento (ex: "0 0 2 * * *")
    private int retentionDays; // Quantidade de dias para reter backups (limpeza automática)
    private boolean automaticCleanupEnabled; // Se a limpeza de retenção está ativa
    private String sourcePath; // O que/onde deve ser backupeado (caminho no servidor/nome do banco)
    private String defaultDestinationId; // O ID do BackupDestination padrão
}
```

### 3\. `BackupRecord` (Registro de Backup)

Representa o histórico de cada backup realizado, crucial para a visualização e aplicação de restaurações.

```java
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Registro de um backup realizado, usado para visualização e restauração.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BackupRecord {
    private Long id;
    private String filename; // Nome exato do arquivo no destino
    private LocalDateTime timestamp; // Data e hora da criação do backup
    private Long sizeBytes; // Tamanho do arquivo em bytes
    private String destinationId; // ID do destino onde o arquivo está (Foreign Key)
    private BackupStatus status; // Enum: SUCCESS, FAILED, IN_PROGRESS
    private String logSummary; // Breve resumo dos logs da operação
}

public enum BackupStatus {
    SUCCESS,
    FAILED,
    IN_PROGRESS
}
```

-----

## 2\. Documentação Completa do Fluxo (Workflow)

O sistema pode ser organizado em 4 módulos de serviço e 3 fluxos de operação principais, focando na **automação** e na usabilidade para **visualizar/aplicar** restaurações.

### A. Módulos de Serviço

| Módulo | Responsabilidade |
| :--- | :--- |
| **Scheduler/Task Manager** | Agenda a rotina de backup (com base no `SystemConfiguration.backupCronExpression`) e a rotina de limpeza de retenção. |
| **Backup Service** | Orquestra as operações: chama a extração, o `StorageManager` e o `Repository`. É o *core* da lógica. |
| **Storage Manager** | Abstrai a comunicação com o destino. Responsável por `upload`, `download`, `list` e `delete` o arquivo físico em diferentes `BackupDestination` (S3, disco, etc.). |
| **Repository** | Persiste/Consulta as entidades (`BackupRecord`, `SystemConfiguration`, `BackupDestination`) em um banco de dados relacional. |

### B. Fluxos de Operação

#### Fluxo 1: Automação e Criação de Backup (O "Fazer")

Este é o fluxo que a automação (`Scheduler`) executa.

| Etapa | Descrição da Ação | Módulo Envolvido |
| :--- | :--- | :--- |
| **1. Início Agendado** | O `Scheduler` dispara a tarefa com base na CRON e carrega o `SystemConfiguration`. | Scheduler, Repository |
| **2. Preparação do Registro**| O `BackupService` cria um novo `BackupRecord` com `status: IN_PROGRESS`. | Backup Service, Repository |
| **3. Extração dos Dados** | O `BackupService` executa a lógica de extração dos dados (ex: roda o `pg_dump` para PostgreSQL, ou comprime o diretório). | Backup Service (lógica de extração) |
| **4. Upload para o Destino** | O `BackupService` aciona o `StorageManager` para realizar o *upload* do arquivo gerado para o destino (`defaultDestinationId`). | Backup Service, Storage Manager |
| **5. Finalização e Log** | Se o upload for bem-sucedido, o `BackupService` atualiza o `BackupRecord` para `status: SUCCESS`, incluindo `sizeBytes` e `filename`. Se falhar em qualquer ponto, atualiza para `status: FAILED` e registra o erro em `logSummary`. | Backup Service, Repository |
| **6. Limpeza de Retenção** | (Fluxo secundário agendado) O `Scheduler` executa a limpeza, pedindo ao `BackupService` para buscar e deletar registros mais antigos que `retentionDays` no `Repository` e os arquivos correspondentes no `Storage Manager`. | Scheduler, Backup Service, Repository, Storage Manager |

#### Fluxo 2: Visualização de Backups (O "Ver")

Este fluxo alimenta a interface do usuário para que os backups possam ser vistos e selecionados.

| Etapa | Descrição da Ação | Módulo Envolvido |
| :--- | :--- | :--- |
| **1. Requisição da UI** | O usuário solicita a lista de backups disponíveis. | Frontend/API |
| **2. Consulta de Registros** | A API de Backend consulta o `Repository` por todos os `BackupRecord`s (opcionalmente filtrando por data ou status). | Repository |
| **3. Exibição** | A UI recebe a lista e exibe, destacando: `filename`, `timestamp`, `status` e `sizeBytes`. Botões de ação (*"Restaurar"*) são ligados ao `id` do `BackupRecord`. | Frontend/API |
| **4. Detalhes do Log** | O usuário pode clicar em um registro para ver o `logSummary` completo. | Repository |

#### Fluxo 3: Aplicação/Restauração de Backup (O "Aplicar")

Este é o fluxo de *restore* acionado pelo usuário, revertendo o sistema para um estado anterior.

| Etapa | Descrição da Ação | Módulo Envolvido |
| :--- | :--- | :--- |
| **1. Seleção e Confirmação**| O usuário seleciona um `BackupRecord` (o ID do backup a ser aplicado). | Frontend/API |
| **2. Verificação de Pré-requisitos** | O `BackupService` verifica se a operação de restauração pode prosseguir (Ex: se os serviços estão parados ou o banco de dados está no modo correto). | Backup Service |
| **3. Download do Arquivo** | O `BackupService` usa o `destinationId` e `filename` do registro selecionado para instruir o `StorageManager` a baixar o arquivo. | Backup Service, Storage Manager |
| **4. Execução da Restauração**| O `BackupService` executa a rotina de restauração usando o arquivo baixado e o `sourcePath` como destino. | Backup Service (lógica de restauração) |
| **5. Finalização** | Após a restauração, o `BackupService` reinicia os serviços e registra o sucesso da operação (pode ser em uma tabela separada de `RestoreRecord`). | Backup Service |
| **6. Notificação** | Envia alerta de **RESTAURAÇÃO CONCLUÍDA** (ou falha) por e-mail/Slack. | Notification Service |