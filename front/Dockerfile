# Usando imagem base do Nginx
FROM nginx:alpine

# Criar a estrutura caso não exista
RUN mkdir -p /usr/share/nginx/html

# Copia os arquivos da build do Angular para o diretório correto
COPY dist/System-backup-front/browser /usr/share/nginx/html

    # Copia a configuração personalizada do Nginx
    COPY nginx.conf /etc/nginx/nginx.conf

# Expondo a porta correta
EXPOSE 80

# Comando para iniciar o Nginx
CMD ["nginx", "-g", "daemon off;"]
