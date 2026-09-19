# Build stage: Mandrel (GraalVM for Quarkus) AOT-compiles the app to a native Linux binary.
# The builder image ships no Gradle and this repo deliberately has no wrapper, so borrow
# the Gradle distribution from the official image; it runs on Mandrel's JDK.
FROM quay.io/quarkus/ubi9-quarkus-mandrel-builder-image:jdk-25 AS build
COPY --from=gradle:9-jdk25 /opt/gradle /opt/gradle
COPY --chown=quarkus:quarkus settings.gradle.kts build.gradle.kts /build/
USER quarkus
WORKDIR /build
# best-effort dependency cache layer; the build run fetches whatever this misses
RUN /opt/gradle/bin/gradle --no-daemon -q dependencies || true
COPY --chown=quarkus:quarkus . .
RUN /opt/gradle/bin/gradle --no-daemon build -x test -Dquarkus.native.enabled=true -Dquarkus.package.jar.enabled=false

# Run stage: minimal glibc base + the native binary; no JVM
FROM quay.io/quarkus/ubi9-quarkus-micro-image:2.0
ENV TZ=Europe/Brussels \
    QUARKUS_HTTP_HOST=0.0.0.0
WORKDIR /app
COPY --from=build /build/build/*-runner application
EXPOSE 8080
USER 1001
# native default max heap scales with host RAM; the app's state is one Match object
CMD ["./application", "-Xmx32m"]
