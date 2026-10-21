# ==============================================================================
# FestPulse Platform - Production Container Image (Task 11)
# Base Image: Eclipse Temurin OpenJDK 21 Runtime on Alpine Linux (Minimal Footprint)
# ==============================================================================

FROM eclipse-temurin:21-jre-alpine

# Set build and runtime metadata
LABEL maintainer="FestPulse DevOps Team <devops@festpulse.internal>" \
      description="Automated Cultural Festival Schedule Platform - Production Container" \
      version="1.0.0"

# Create a dedicated non-root system group and user for security best practices
RUN addgroup -S spring && adduser -S spring -G spring

# Set isolated working directory
WORKDIR /app

# Copy the executable Spring Boot fat JAR from the Maven build target directory
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

# Enforce non-root ownership on application files
RUN chown -R spring:spring /app

# Switch runtime execution context to non-root user
USER spring:spring

# Expose HTTP service port
EXPOSE 8080

# Configure JVM container resource awareness and standard entrypoint
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
