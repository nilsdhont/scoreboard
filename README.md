# scoreboard project

The scoreboard app for rugbyclub BrigandZe.
It consumes the api from an app called sporteasy to update our scoreboard


# Login to Sporteasy
Add a file 'sporteasy.env' in the resources folder with 2 properties:
username=yourusername
password=yourpassword

# Framework

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```
./mvnw quarkus:dev
```
To run with the webapp (when it is build, see readme webapp)
```
mvn compile quarkus:dev
```

## Packaging and running the application

The application can be packaged using `./mvnw package`.
It produces the `scoreboard-1.0-SNAPSHOT-runner.jar` file in the `/target` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/lib` directory.

The application is now runnable using `java -jar target/scoreboard-1.0-SNAPSHOT-runner.jar`.

## Creating a native executable

You can create a native executable using: `./mvnw package -Pnative`.

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: `./mvnw package -Pnative -Dquarkus.native.container-build=true -Dmaven.test.skip`.

You can then execute your native executable with: `./target/scoreboard-1.0-SNAPSHOT-runner`

Or after building the executable, you can build a Docker image and run it:
```
docker build -f src/main/docker/Dockerfile.native -t quarkus/scoreboard .
docker run -i --rm -p 8080:80 quarkus/scoreboard
```

If you want to learn more about building native executables, please consult https://quarkus.io/guides/building-native-image.
