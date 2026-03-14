# Etap 1: Budowanie (Maven)
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Kopiujemy plik konfiguracyjny i pobieramy zależności (lepszy cache)
COPY pom.xml .
RUN mvn dependency:go-offline

# Kopiujemy kod i budujemy paczkę
COPY src ./src
RUN mvn clean package -DskipTests

# Etap 2: Środowisko uruchomieniowe (JRE)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Kopiujemy tylko gotowy plik JAR z poprzedniego etapu
COPY --from=build /app/target/*.jar app.jar

# Opcjonalne: Ustawienie portu (standard dla Springa to 8080)
EXPOSE 8080

# Uruchomienie aplikacji
ENTRYPOINT ["java", "-jar", "app.jar"]