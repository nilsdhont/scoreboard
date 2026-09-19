# Build stage
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /build
COPY pom.xml .
# best-effort dependency cache layer; the package run fetches whatever this misses
RUN mvn -B -q dependency:go-offline || true
COPY . .
RUN mvn -B package -DskipTests

# Run stage: single uber-jar serves the webapp and the REST backend on 8080
FROM eclipse-temurin:25-jre
ENV TZ=Europe/Brussels \
    QUARKUS_HTTP_HOST=0.0.0.0 \
    QUARKUS_LOG_FILE_ENABLED=false
WORKDIR /app
COPY --from=build /build/target/*-runner.jar app.jar
EXPOSE 8080
USER 1001
CMD ["java", "-jar", "app.jar"]
