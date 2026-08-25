# =========================
# STAGE 1: BUILD
# =========================
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Cache de dependencias
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Código fuente
COPY src ./src

# Compilación
RUN mvn clean package -DskipTests

# =========================
# STAGE 2: RUNTIME
# =========================
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", \
            "-XX:+UseContainerSupport", \
            "-XX:MaxRAMPercentage=75.0", \
            "-XX:+ExitOnOutOfMemoryError", \
            "-jar", \
            "app.jar"]