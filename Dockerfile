# Build stage
FROM gradle:9-jdk25 AS build
WORKDIR /build
COPY settings.gradle.kts build.gradle.kts ./
# best-effort dependency cache layer; the build run fetches whatever this misses
RUN gradle --no-daemon -q dependencies || true
COPY . .
RUN gradle --no-daemon build -x test

# Run stage: fast-jar layout, dependency layer copied separately so app-only
# rebuilds push/pull just the few KB of classes instead of all the libs
FROM eclipse-temurin:25-jre-alpine
ENV TZ=Europe/Brussels \
    QUARKUS_HTTP_HOST=0.0.0.0
WORKDIR /app
COPY --from=build /build/build/quarkus-app/lib/ lib/
COPY --from=build /build/build/quarkus-app/*.jar ./
COPY --from=build /build/build/quarkus-app/app/ app/
COPY --from=build /build/build/quarkus-app/quarkus/ quarkus/
EXPOSE 8080
USER 1001
# app state is a single Match object; tiny fixed heap + SerialGC keep RSS low
CMD ["java", "-Xms64m", "-Xmx64m", "-XX:+UseSerialGC", "-jar", "quarkus-run.jar"]
