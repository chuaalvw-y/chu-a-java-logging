# syntax=docker/dockerfile:1.7
# Multi-stage build for the orders-service example consuming chua-erp-platform-logging-core.
#
# Build from the repo root so the Dockerfile sees the multi-project layout:
#   docker build -f platform-logging-example/Dockerfile -t orders-service:dev .
#
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY . .
RUN chmod +x gradlew && ./gradlew --no-daemon :platform-logging-example:bootJar

FROM eclipse-temurin:21-jre
ARG APP_USER=app
RUN useradd --create-home --shell /bin/false ${APP_USER}
WORKDIR /opt/app
COPY --from=build /workspace/platform-logging-example/build/libs/*.jar app.jar
USER ${APP_USER}

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0" \
    ENVIRONMENT=prod \
    SERVER_PORT=8080

EXPOSE 8080

# Logback is configured to emit JSON to stdout in non-local profiles — perfect for
# the container runtime to forward via the node-level log collector.
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
