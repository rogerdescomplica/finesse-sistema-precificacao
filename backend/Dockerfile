# Stage 1: Build
FROM maven:3.9.12-eclipse-temurin-25 AS builder
WORKDIR /app

COPY . .
RUN mvn -q -DskipTests clean package

# Stage 2: Runtime
FROM eclipse-temurin:25-jre
WORKDIR /app

ENV SERVER_PORT=8080
ENV SPRING_PROFILES_ACTIVE=prod

COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Xms256m", "-Xmx512m", "-jar", "app.jar"]