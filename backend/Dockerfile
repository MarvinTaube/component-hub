FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Ensure Gradle wrapper has execute permissions when copied to Linux alpine container
COPY gradle gradle
COPY gradlew .
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY src src

RUN chmod +x gradlew
RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

# Target mapped volume configured in the docker-compose schema
VOLUME /app/data

EXPOSE 8080

CMD ["java", "-Dapp.data-dir=/app/data/", "-jar", "app.jar"]
