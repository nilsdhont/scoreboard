# Build stage
FROM gradle:9-jdk25 AS build
WORKDIR /build
COPY settings.gradle.kts build.gradle.kts ./
# best-effort dependency cache layer; the build run fetches whatever this misses
RUN gradle --no-daemon -q dependencies || true
COPY . .
RUN gradle --no-daemon build -x test

# Run stage: single uber-jar serves the webapp and the REST backend on 8080
FROM eclipse-temurin:25-jre
ENV TZ=Europe/Brussels \
    QUARKUS_HTTP_HOST=0.0.0.0 \
    QUARKUS_LOG_FILE_ENABLED=false
WORKDIR /app
COPY --from=build /build/build/*-runner.jar app.jar
EXPOSE 8080
USER 1001
CMD ["java", "-jar", "app.jar"]
