# Multi-stage build for Java CRM
FROM gradle:8.7-jdk21 AS builder

WORKDIR /app

# Copy Gradle files
COPY build.gradle settings.gradle gradle.properties versions.toml ./
COPY gradle ./gradle

# Copy all module source files
COPY . .

# Build the application
RUN gradle clean build -x test --no-daemon

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy built JAR
COPY --from=builder /app/app/build/libs/java-crm.jar app.jar

# Expose ports
EXPOSE 8080 9090

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
