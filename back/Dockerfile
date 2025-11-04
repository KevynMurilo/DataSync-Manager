# ----------------------------------------------------------------------
# ETAPA 1: BUILD (Compilação do Projeto)
# ----------------------------------------------------------------------
FROM eclipse-temurin:21-jdk-alpine AS builder

# Define o diretório de trabalho
WORKDIR /app

# 1. Copia o Maven Wrapper e a pasta de suporte
COPY mvnw .
COPY .mvn .mvn

# 2. Garante que o script 'mvnw' seja executável (RESOLVE O ERRO: ./mvnw: not found)
RUN chmod +x mvnw

# 3. Copia os arquivos de build
COPY pom.xml .
COPY src ./src

# 4. Executa o build para gerar o JAR
RUN ./mvnw clean package -DskipTests


# ----------------------------------------------------------------------
# ETAPA 2: PROD (Imagem Final de Execução)
# ----------------------------------------------------------------------
FROM eclipse-temurin:21-jdk-alpine

# Define o diretório de trabalho
WORKDIR /app

# Corrigido: Usa o nome do JAR do pom.xml (artifactId: backup)
COPY --from=builder /app/target/backup-0.0.1-SNAPSHOT.jar app.jar

# Expõe a porta
EXPOSE 8082

# Define o comando de inicialização
ENTRYPOINT ["java", "-jar", "app.jar"]