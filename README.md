# 💾 DataSync-Manager: O Seu Gerenciador Open Source de Backups Automatizados

## ✨ Segurança e Controle Total sobre os Dados da Sua Organização

O **DataSync-Manager** é uma solução **Open Source** completa e poderosa para a gestão centralizada de rotinas de backup de bancos de dados. Desenvolvido para oferecer **confiabilidade, flexibilidade e segurança**, ele automatiza o processo de extração (*dump*) de dados e os envia para múltiplos destinos, garantindo que a recuperação de desastres seja um processo simples e rápido.

## 🎯 Proposta de Valor e Para que Serve

O projeto nasceu da necessidade de gerenciar, de forma eficiente e transparente, o backup de sistemas heterogêneos.

  * **Evite Perdas de Dados:** Configure backups automáticos para nunca mais se preocupar com falhas humanas ou de *hardware*.
  * **Centralização:** Administre todas as suas fontes de dados (**PostgreSQL, MySQL, H2, Oracle, SQL Server, MariaDB, MongoDB**) e destinos de armazenamento (Local, Nuvem, FTP) a partir de um único painel de controle.
  * **Conformidade e Auditoria:** Mantenha um histórico detalhado (*log*) de todas as execuções, garantindo a rastreabilidade e o cumprimento de políticas de retenção.

-----

## 🚀 Funcionalidades Chave

Com uma **Interface de Usuário (Frontend em Angular)** intuitiva e um **Motor de Execução (Backend em Spring Boot)** assíncrono e resiliente, o DataSync-Manager oferece:

### 1\. Suporte a Múltiplos Bancos de Dados

| Tipo de Banco | Compatibilidade |
| :--- | :--- |
| **SQL** | PostgreSQL, MySQL, H2, Oracle, SQL Server, MariaDB |
| **NoSQL** | MongoDB |

### 2\. Multi-Destino de Backup

Você pode enviar um único backup para múltiplos locais simultaneamente, garantindo redundância (a famosa regra 3-2-1):

  * **Cloud Storage:** **Amazon S3** e **Google Cloud Storage (GCS)**, com criptografia das credenciais.
  * **Protocolos:** **FTP** (File Transfer Protocol).
  * **Local/Rede:** Armazenamento em disco local ou em *endpoints* de rede (Local Disk).

### 3\. Agendamento e Execução

  * **Agendamento Flexível:** Defina rotinas de backup como **Diárias**, **Semanais** ou **Manuais**.
  * **Monitoramento em Tempo Real:** Dashboard (via **WebSocket**) para acompanhar o *status* das execuções e a saúde geral do sistema.
  * **Restauração Simples:** Funcionalidade de um clique para iniciar o processo de **restauração** a partir de qualquer registro de histórico.

### 4\. Segurança e Notificações

  * **Criptografia de Credenciais:** As chaves de acesso e senhas de banco de dados são criptografadas no banco de dados interno (usando `CryptoConverter` em Spring).
  * **Notificações por E-mail:** Configure conexões **SMTP** e defina políticas de notificação (**Sempre** ou **Apenas em Caso de Falha**) para os *jobs* críticos.
  * **Controle de Acesso:** Sistema de autenticação robusto (via **JWT** e **Spring Security**) com gestão de usuários e obrigatoriedade de troca de senha inicial (`mustChangePassword`).

-----

## 🛠️ Tecnologias Principais

| Componente | Tecnologia | Detalhes Relevantes |
| :--- | :--- | :--- |
| **Backend** | **Spring Boot** (Java) | Fornece a API RESTful, o motor de agendamento e a lógica de comunicação com os bancos e *storages*. Utiliza **Spring Security** para autenticação (JWT) e **JPA/Hibernate**. |
| **Frontend** | **Angular** | Interface de usuário (SPA - Single Page Application) moderna, interativa e responsiva para gerenciar as configurações. |
| **Comunicação** | **WebSocket / STOMP** | Utilizado para *push* de notificações e *status* de execução em tempo real para o *dashboard*. |
| **Documentação API** | **OpenAPI (Swagger)** | Documentação automática da API para facilitar o desenvolvimento e integração. |
| **Storage Connectors**| **AWS SDK (S3-Compatible)** e **Apache Commons Net (FTP)** | Bibliotecas utilizadas para a comunicação segura com serviços de nuvem e FTP. |

-----

## ⚙️ Como Usar e Configurar

### 1\. Pré-requisitos

Certifique-se de ter instalado:

  * **Java JDK [Versão Compatível com Spring Boot]**
  * **Node.js / NPM / Angular CLI**
  * **Docker e Docker Compose** (Recomendado para ambientes de desenvolvimento/produção)

### 2\. Configuração de Variáveis de Ambiente

O projeto requer chaves de segurança críticas. É fundamental configurar a chave de criptografia AES no Backend para proteger credenciais sensíveis (senhas de banco e chaves de acesso S3/GCS):

> **Atenção:** A chave `app.crypto.key` deve ter **exatamente 16 caracteres** (padrão AES-128).
>
> ```ini
> # Exemplo em application.properties/application.yml
> app.crypto.key=[SUA_CHAVE_SECRETA_DE_16_CARACTERES_UNICA]
> ```

### 3\. Execução (via Docker Compose)

A maneira mais prática é usar o Docker Compose para subir o ambiente completo:

```bash
# 1. Ajuste o arquivo docker-compose.yml e .env com suas configurações
# 2. Suba os serviços (o Docker irá construir e iniciar tudo):
docker-compose up -d --build
```

  * **Acesso ao Frontend:** `http://localhost:[Porta Angular, ex: 4200]`
  * **Acesso ao Backend/API:** `http://localhost:[Porta Spring, ex: 8080]/api/auth/login`

### 4\. Configuração Inicial Pós-Execução

Na primeira inicialização, o sistema cria automaticamente um usuário administrativo.

| Usuário Padrão | Detalhe |
| :--- | :--- |
| **E-mail:** `admin@admin.com` | Usuário para o primeiro acesso. |
| **Senha:** `admin` | Senha inicial. |

> **Aviso de Segurança:** Por segurança, este usuário inicial é forçado a trocar email e senha no primeiro login. Certifique-se de realizar esta troca imediatamente.

### 5\. Banco de Dados Interno (H2)

Para ambientes de **desenvolvimento e teste**, o Backend utiliza o banco de dados em memória **H2** para armazenar todas as configurações de *jobs*, fontes, destinos e usuários.

  * **Persistência:** As configurações são salvas em um arquivo no volume do Docker (ou no diretório do usuário local, dependendo da sua configuração de volume no `docker-compose.yml`).
  * **Produção:** Para ambientes de **produção**, é altamente recomendável migrar para um banco de dados robusto e persistente, como **PostgreSQL** ou **MySQL**, alterando a configuração no `application.properties/yml`.

### 6\. Configuração no Painel do Frontend

Após o acesso, siga estas etapas no painel do **Angular**:

1.  **Crie uma Configuração de E-mail (Opcional, mas Recomendado):** Vá em **Configurações \> E-mail** para adicionar seus dados SMTP. Isso é essencial para receber alertas de falha.
2.  **Configure o Destino(s) de Backup:** Vá em **Configurações \> Destinos**. Adicione seus *endpoints* S3, GCS, FTP ou um caminho de disco local. Use o botão **"Testar Conexão"** para validar.
3.  **Configure a(s) Fonte(s) de Backup:** Vá em **Configurações \> Fontes**. Adicione os dados de conexão do seu banco (Host, User, Password, Caminho da Ferramenta de *Dump* como `pg_dump` ou `mysqldump`). Use o **"Testar Conexão"** para validar o acesso ao banco.
4.  **Crie um Job de Backup:** Vá em **Jobs de Backup**. Crie um novo *job*, associe uma **Fonte** e um ou mais **Destinos**, defina o **Agendamento** e a **Política de Notificação**.

-----

## 🤝 Contribuição e Comunidade (Open Source)

O **DataSync-Manager** foi construído como um projeto *open source* e depende da sua contribuição para evoluir\!

### Como Ajudar

  * **Novos Bancos de Dados:** Adicionar suporte a novos tipos de bancos (ex: SQLite, Redis).
  * **Novos Destinos:** Integrar outros serviços de *Cloud* (ex: Azure Blob Storage, Dropbox).
  * **Melhorias na Interface:** Aprimorar a usabilidade e o design do painel em Angular.
  * **Documentação:** Melhorar os guias de instalação e uso.

### Passos para Contribuir

1.  Faça um *fork* do projeto: `https://github.com/KevynMurilo/DataSync-Manager/fork`
2.  Crie uma nova *branch* para sua funcionalidade (`git checkout -b feat/melhoria-do-dashboard`).
3.  Implemente sua alteração e faça o *commit* (`git commit -m 'feat: Adiciona gráfico de taxa de sucesso'`).
4.  Abra um **Pull Request** para a *branch* principal (`main`), descrevendo claramente a sua contribuição.

-----

## 📄 Licença

Este projeto é *open source* e está licenciado sob a **[INSERIR LICENÇA AQUI, ex: MIT License]**. Consulte o arquivo [LICENSE](https://www.google.com/search?q=LICENSE) na raiz do repositório para mais detalhes.

## 🧑‍💻 Autor e Mantenedor

| [\<img src="https://avatars.githubusercontent.com/u/SEU\_ID\_DO\_GITHUB?v=4" width="100px;"/\>](https://www.google.com/search?q=https://github.com/KevynMurilo) | |
| :--- | :--- |
| **Kevyn Murilo** | Criador e Mantenedor Principal do DataSync-Manager. |
| | **GitHub:** [@KevynMurilo](https://www.google.com/search?q=https://github.com/KevynMurilo) |