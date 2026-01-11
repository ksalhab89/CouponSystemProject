# Stage 1: Build
FROM maven:3-eclipse-temurin-25 AS build
WORKDIR /app

# Copy dependency files first (for better caching)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build
COPY src ./src
RUN mvn clean package -DskipTests

# Extract Spring Boot layers for better optimization
RUN java -Djarmode=layertools -jar target/CouponSystemProject-1.0-SNAPSHOT.jar extract

# Stage 2: Runtime with minimal alpine JRE (optimized with layers)
FROM eclipse-temurin:25-jre-alpine

# Install curl for healthchecks (adds ~1MB)
RUN apk add --no-cache curl

# Create non-root user
RUN addgroup -g 1001 appuser && \
    adduser -D -u 1001 -G appuser appuser

WORKDIR /app

# Create logs directory with proper ownership
RUN mkdir -p /app/logs && chown -R appuser:appuser /app/logs

# Copy extracted layers from Spring Boot (better caching and smaller final image)
COPY --from=build --chown=appuser:appuser /app/dependencies/ ./
COPY --from=build --chown=appuser:appuser /app/spring-boot-loader/ ./
COPY --from=build --chown=appuser:appuser /app/snapshot-dependencies/ ./
COPY --from=build --chown=appuser:appuser /app/application/ ./

# Switch to non-root user
USER appuser

# Expose REST API and Prometheus metrics ports
EXPOSE 8080 9090

# Run Spring Boot app with optimized JVM settings for containers
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-XX:+UseG1GC", \
    "-XX:MaxGCPauseMillis=200", \
    "-Dlogback.configurationFile=logback-json.xml", \
    "org.springframework.boot.loader.launch.JarLauncher"]
