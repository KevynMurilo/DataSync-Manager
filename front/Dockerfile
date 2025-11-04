# Etapa 1: Build da aplicação Angular
FROM node:18-alpine as build

# Diretório de trabalho
WORKDIR /app

# Copia os arquivos do projeto Angular
COPY package*.json ./
RUN npm install

COPY . .

# Executa o build de produção
RUN npm run build -- --configuration=production

# Etapa 2: Servir com Nginx
FROM nginx:alpine

# Cria o diretório caso não exista
RUN mkdir -p /usr/share/nginx/html

# Copia os arquivos da build do Angular
COPY --from=build /app/dist/System-backup-front/browser /usr/share/nginx/html

# Copia a configuração personalizada do Nginx
COPY nginx.conf /etc/nginx/nginx.conf

# Expondo a porta correta
EXPOSE 80

# Comando para iniciar o Nginx
CMD ["nginx", "-g", "daemon off;"]
