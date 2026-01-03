# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy dependency files first (for better caching)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

# Create non-root user
RUN addgroup -g 1001 appuser && \
    adduser -D -u 1001 -G appuser appuser

WORKDIR /app

# Copy Spring Boot JAR as non-root user
COPY --from=build --chown=appuser:appuser \
    /app/target/CouponSystemProject-1.0-SNAPSHOT.jar \
    app.jar

# Switch to non-root user
USER appuser

# Create directories for logs (writable by appuser)
USER root
RUN mkdir -p /app/logs && chown -R appuser:appuser /app/logs
USER appuser

# Health check - uses REST API health endpoint
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# JVM configuration for containers with JSON logging
ENV JAVA_OPTS="-XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:+UseG1GC \
    -XX:MaxGCPauseMillis=200 \
    -XX:+HeapDumpOnOutOfMemoryError \
    -XX:HeapDumpPath=/app/logs/heapdump.hprof \
    -Xlog:gc*:file=/app/logs/gc.log:time,uptime:filecount=5,filesize=10m \
    -Dlogback.configurationFile=logback-json.xml"

# Expose REST API and Prometheus metrics ports
EXPOSE 8080 9090

CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
