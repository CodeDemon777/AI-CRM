# ==============================================================================
# Root Dockerfile for Render Deployments (when Root Directory is left as repository root)
# ==============================================================================
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app

# Cache backend dependencies
COPY backend/pom.xml .
RUN mvn dependency:go-offline -B || true

# Copy backend source code and build jar
COPY backend/src ./src
RUN mvn clean package -DskipTests

# ==============================================================================
# Stage 2: Minimal runtime image with Eclipse Temurin 25 JRE
# ==============================================================================
FROM eclipse-temurin:25-jre
WORKDIR /app

# Copy built jar from build stage
COPY --from=build /app/target/*.jar app.jar

ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Dserver.port=${PORT} -jar app.jar"]
