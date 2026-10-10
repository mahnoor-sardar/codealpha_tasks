# syntax=docker/dockerfile:1

FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /workspace

COPY gradlew gradlew
COPY gradle gradle
COPY settings.gradle gradle.properties ./
COPY app/build.gradle app/build.gradle
COPY gradle/libs.versions.toml gradle/libs.versions.toml
COPY app/src app/src

RUN chmod +x gradlew \
    && ./gradlew --no-daemon :app:bootJar -x test

FROM eclipse-temurin:21-jre-jammy AS runtime
WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system app \
    && useradd --system --gid app --home-dir /app --shell /usr/sbin/nologin app

COPY --from=build /workspace/app/build/libs/deployment-tracker.jar /app/app.jar

USER app

EXPOSE 8080

ENV SERVER_PORT=8080 \
    APP_SEED_DATA=false \
    JAVA_OPTS="-XX:MaxRAMPercentage=75.0"

# Probe the container listen port (keep SERVER_PORT=8080 inside the image; map a different host port if needed).
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
    CMD curl -fsS http://127.0.0.1:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
