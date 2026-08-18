# --- Etapa 1: build ---
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copiamos primero el pom.xml para aprovechar la cache de Docker
# (si no cambian las dependencias, no las vuelve a descargar en cada build)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Ahora copiamos el código fuente y compilamos
COPY src ./src
RUN mvn clean package -DskipTests

# --- Etapa 2: imagen final, ligera ---
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/backend-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

#CMD ["nginx", "-g", "daemon off;"]

ENTRYPOINT ["java","-Dspring.profiles.active=prod","-jar","app.jar"]