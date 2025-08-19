# ====== STAGE 1: BUILD ======
FROM gradle:8.10.2-jdk21 AS build
WORKDIR /app

# Copiamos solo lo necesario para cache
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle
COPY src ./src

# Construye el JAR ejecutable de Spring Boot
RUN gradle bootJar --no-daemon

# ====== STAGE 2: RUNTIME ======
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENV SERVER_PORT=8080

ENTRYPOINT ["java","-jar","/app/app.jar"]
