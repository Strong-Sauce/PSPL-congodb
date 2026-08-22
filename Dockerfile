# =========================
# Stage 1: Build application
# =========================
FROM maven:3.9.16-eclipse-temurin-25 AS builder

WORKDIR /app

COPY pom.xml .
COPY frontend ./frontend
COPY src ./src

RUN mvn -DskipTests clean package


# =========================
# Stage 2: Run application
# =========================
FROM eclipse-temurin:25-jre

WORKDIR /app

COPY --from=builder /app/target/PSPLProject-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 10000

ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-10000} -jar app.jar"]