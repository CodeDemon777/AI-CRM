# ==============================================================================
# Stage 1: Build Spring Boot with Maven and Eclipse Temurin 25 (Java 25)
# ==============================================================================
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B || true

# Copy source code and build jar
COPY src ./src
RUN mvn clean package -DskipTests

# ==============================================================================
# Stage 2: Minimal runtime image with Eclipse Temurin 25 JRE
# ==============================================================================
FROM eclipse-temurin:25-jre
WORKDIR /app

# Copy built jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Render injects the PORT environment variable (defaults to 8080)
ENV PORT=8080
EXPOSE 8080

# Run Spring Boot with container-aware memory limits and Render PORT
ENTRYPOINT ["sh", "-c", "java -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Dserver.port=${PORT} -jar app.jar"]
