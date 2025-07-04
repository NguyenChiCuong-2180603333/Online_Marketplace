
FROM eclipse-temurin:17-jdk-jammy AS build

# Install Maven
RUN apt-get update && \
    apt-get install -y maven && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy pom.xml và download dependencies trước (Docker layer caching)
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Make mvnw executable
RUN chmod +x mvnw

# Download dependencies (cache layer)
RUN ./mvnw dependency:go-offline -B --no-transfer-progress

# Copy source code
COPY src src

# Build application (skip tests for faster build)
RUN ./mvnw clean package -DskipTests -B --no-transfer-progress

# Production stage với JRE
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Create non-root user cho security
RUN addgroup --system spring && adduser --system spring --ingroup spring

# Copy JAR file từ build stage
COPY --from=build /app/target/*.jar app.jar

# Create upload directory
RUN mkdir -p /tmp/uploads && chown -R spring:spring /tmp/uploads

# Switch to non-root user
USER spring:spring

# Expose port
EXPOSE 8080

# Health check cho Render
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run với memory optimized settings cho 512MB container
ENTRYPOINT ["java", "-Xms256m", "-Xmx450m", "-XX:+UseG1GC", "-XX:+UseContainerSupport", "-jar", "/app/app.jar"]