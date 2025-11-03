# 📖 README de Teste: API de Automação de Backups

O objetivo deste guia é testar o fluxo completo da aplicação Spring Boot de Backup: desde a configuração das credenciais até a execução automatizada e a restauração.

## 0\. Pré-requisitos para Execução

Antes de iniciar, certifique-se de que o ambiente de execução (onde a API será iniciada) possui:

1.  **Java 17+** e **Maven/Gradle** para build.
2.  **Spring Boot Application** já compilada e rodando (porta `8082`).
3.  **H2 Console** acessível em `http://localhost:8082/h2-console` para monitorar as entidades.
4.  **Ferramentas Nativas de Backup** instaladas e acessíveis (ex: `mysqldump` ou `pg_dump`).
5.  **Diretórios de Teste** criados no servidor da aplicação (ex: `C:\temp\backup_local` para Windows ou `/tmp/backup_local` para Linux).

-----

## 1\. Configuração Inicial e Rotas CRUD

O sistema começa vazio. A primeira etapa é criar o destino de armazenamento e definir as configurações globais.

### 1.1. Teste de Destinos de Backup (`/api/backup-destination`)

Use `UUID.randomUUID()` para gerar o ID para cada novo destino.

| Ação | Rota (Método) | Descrição |
| :--- | :--- | :--- |
| **Criação** | `POST /api/backup-destination` | Crie o destino local (sua máquina). |
| **Consulta** | `GET /api/backup-destination` | Verifique o `ID` do destino criado. |
| **Atualização**| `PUT /api/backup-destination/{id}` | Altere o nome do destino. |

**Exemplo de DTO: Destino Local (Sua Máquina/Servidor)**

Este exemplo é para salvar o backup em um diretório acessível pelo Spring Boot.

```json
{
  "name": "Servidor Local Formosa",
  "type": "LOCAL_DISK",
  "endpoint": "C:\\temp\\backup_local",
  "region": null,
  "accessKey": null,
  "secretKey": null,
  "isActive": true
}
```

**Exemplo de DTO: Destino Cloud (Google Cloud Storage)**

```json
{
  "name": "Google Drive (Producao)",
  "type": "GOOGLE_CLOUD_STORAGE",
  "endpoint": "nome-do-seu-bucket-ou-folder-id",
  "region": "us-central1",
  "accessKey": "YOUR_GCP_ACCESS_KEY",
  "secretKey": "YOUR_GCP_SECRET_KEY",
  "isActive": true
}
```

### 1.2. Configuração Global (`/api/system-config`)

Após criar o destino local (`ID: 1d8b6f3c-5e4a-4d2b-8a1c-0f9e8d7c6b5a`), configure o sistema.

| Ação | Rota (Método) | Descrição |
| :--- | :--- | :--- |
| **Criação** | `POST /api/system-config` | Salve as regras de automação e as credenciais do banco. |
| **Consulta** | `GET /api/system-config` | Verifique a persistência. |

**Exemplo de DTO: Configuração do Sistema (PostgreSQL)**

*Ajuste `dbDumpToolPath` e `dbConnectionString` para o seu ambiente (MySQL, PostgreSQL ou outro).*

```json
{
  "backupCronExpression": "0 0 2 * * *",
  "retentionDays": 7,
  "automaticCleanupEnabled": true,
  "sourcePath": "postgres",
  "defaultDestinationId": "1d8b6f3c-5e4a-4d2b-8a1c-0f9e8d7c6b5a",
  "dbConnectionString": "localhost",
  "dbUser": "postgres",
  "dbPassword": "123",
  "dbDumpToolPath": "C:\\Program Files\\PostgreSQL\\15\\bin\\pg_dump.exe"
}
```

-----

## 2\. Teste de Execução de Backup (`/api/backup`)

### 2.1. Execução Manual

Dispare o backup e observe o resultado.

| Ação | Rota (Método) | Descrição |
| :--- | :--- | :--- |
| **Disparo (Padrão)** | `POST /api/backup/execute` | Inicia o backup usando o `defaultDestinationId` (Local Disk). Corpo deve ser vazio (`{}`). |
| **Disparo (Customizado)** | `POST /api/backup/execute` | Inicia o backup usando um destino diferente do padrão (ex: Cloud). |
| **Validação**| `GET /api/backup/history` | Confirme o novo registro de backup. |

**Exemplo de DTO: Disparo com Destino Customizado**

Use este DTO se quiser forçar o backup para o destino Cloud (ID `9a0c1b2d-3e4f-5a6b-7c8d-9e0f1a2b3c4d`).

```json
{
  "customDestinationId": "9a0c1b2d-3e4f-5a6b-7c8d-9e0f1a2b3c4d",
  "ignoreSchedule": false
}
```

**Validação Externa de Sucesso:**

1.  Verifique o diretório local (`C:\temp\backup_local`) para o arquivo `.sql` gerado.
2.  Verifique o log para o `logSummary` com `status: SUCCESS`.

### 2.2. Teste de Falha (Simulação)

1.  Altere a `dbPassword` em `/api/system-config` para um valor incorreto.
2.  Dispare o backup (`POST /api/backup/execute` com corpo vazio).
3.  Verifique o `GET /api/backup/history`: um novo registro deve ter **`status: FAILED`** e o `logSummary` deve conter o erro de autenticação do `mysqldump`.
4.  Restaure a configuração correta.

-----

## 3\. Teste de Restauração (`/api/backup`)

**Pré-condição:** Você deve ter um `BackupRecord` com **`status: SUCCESS`** e saber seu `id` (Long).

### 3.1. Processo de Restauração

| Ação | Rota (Método) | Descrição |
| :--- | :--- | :--- |
| **Restauração**| `POST /api/backup/restore/{recordId}` | O sistema irá: 1. Baixar o arquivo do destino. 2. Executar o comando de restauração (`mysql`, `psql`). 3. Limpar o arquivo temporário. |

**Regras de Negócio Testadas:**

* Se o status for `FAILED` ou `IN_PROGRESS`, a API deve retornar um erro (`IllegalStateException`).
* Após a execução, o banco de dados de destino deve refletir o estado do arquivo `.sql`.

-----

## 4\. Teste de Automação e Limpeza

### 4.1. Limpeza de Retenção (`SchedulerService`)

Teste a regra de `retentionDays` para garantir que o sistema remove backups antigos automaticamente.

1.  **Configuração:** Altere `/api/system-config` para:
    * `retentionDays`: `1` (Um dia de retenção).
    * `automaticCleanupEnabled`: `true`.
2.  **Simulação de Idade:** No banco de dados H2 (via console), altere o campo `timestamp` de um `BackupRecord` com sucesso para uma data de **dois dias atrás**.
3.  **Aguarde:** Aguarde no máximo 5 minutos (intervalo do `@Scheduled`).
4.  **Verificação:**
    * `GET /api/backup/history`: O registro simulado deve ter sido **deletado**.
    * Verifique o diretório `C:\temp\backup_local` (ou destino Cloud) para confirmar a exclusão do arquivo.

### 4.2. Fluxo Completo Automatizado

Para testar o agendamento completo, você precisaria de um agendador dinâmico. Se você usar o `@Scheduled` fixo do `SchedulerService`, defina um `fixedRate` curto ou altere o código para testar a execução.

1.  **Observação:** Monitore os logs da aplicação no horário definido pelo `backupCronExpression` (se você implementar o agendamento dinâmico) ou no intervalo do `fixedRate`.
2.  **Confirmação:** Um novo `BackupRecord` com `status: SUCCESS` deve aparecer no histórico a cada execução do agendador.