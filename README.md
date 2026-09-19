# scoreboard project

The scoreboard app for rugbyclub BrigandZe.
It consumes the api from an app called sporteasy to update our scoreboard


# Login to Sporteasy
Create a `.env` file in the project root (dev) or next to `docker-compose.yml` on the server:
```
SPORTEASY_USERNAME=yourusername
SPORTEASY_PASSWORD=yourpassword
```
Quarkus picks it up automatically in dev mode; Docker injects it via `env_file`.

# Running with Docker
Every push to master publishes `ghcr.io/nilsdhont/scoreboard:latest` (webapp + backend in one image, port 8080).

On the server (needs `docker-compose.yml` and `.env` in the same directory):
```
docker compose pull && docker compose up -d
```
If the GHCR package is private, `docker login ghcr.io` once with a token that has `read:packages`.

To build locally instead: `docker compose up -d --build`

# Framework

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```
./mvnw quarkus:dev
```
The scoreboard page is a static `index.html` in `src/main/resources/META-INF/resources` — no frontend build needed.

## Packaging and running the application

The application can be packaged using `./mvnw package`.
It produces the `scoreboard-1.0-SNAPSHOT-runner.jar` file in the `/target` directory (an _über-jar_, see `quarkus.package.jar.type` in application.properties).

The application is now runnable using `java -jar target/scoreboard-1.0-SNAPSHOT-runner.jar`.
