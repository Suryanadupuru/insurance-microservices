# ─── Stage 1: Build ───────────────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /build

# Copy parent POM first (for dependency caching)
COPY pom.xml .
COPY eureka-server/pom.xml eureka-server/

# Download dependencies (cached if pom.xml unchanged)
RUN mvn dependency:go-offline -pl eureka-server -am -q

# Copy source and build
COPY eureka-server/src eureka-server/src
RUN mvn clean package -pl eureka-server -am -DskipTests -q

# ─── Stage 2: Runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-jammy

LABEL maintainer="insurance-team"
LABEL service="eureka-server"
LABEL version="1.0.0"

WORKDIR /app

# Create non-root user for security
RUN groupadd --system appgroup && \
    useradd --system --gid appgroup --shell /bin/false appuser

# Create log directory
RUN mkdir -p /app/logs && chown -R appuser:appgroup /app

# Copy JAR from builder stage
COPY --from=builder /build/eureka-server/target/eureka-server-*.jar app.jar

# Switch to non-root user
USER appuser

# Expose Eureka port
EXPOSE 8761

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8761/actuator/health || exit 1

# JVM tuning for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:+UseG1GC \
               -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
