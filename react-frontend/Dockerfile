# Etap 1: Budowanie aplikacji React
FROM node:20-alpine AS build
WORKDIR /app

# Kopiujemy package.json i package-lock.json
COPY package*.json ./
RUN npm ci

# Kopiujemy kod źródłowy i budujemy
COPY . .
RUN npm run build

# Etap 2: Nginx do serwowania
FROM nginx:alpine
WORKDIR /usr/share/nginx/html

# Usuwamy domyślne pliki nginx
RUN rm -rf ./*

# Kopiujemy zbudowaną aplikację
COPY --from=build /app/dist .

# Kopiujemy konfigurację nginx
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]