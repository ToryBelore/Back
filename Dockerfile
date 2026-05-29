FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app
COPY gradlew gradlew.bat ./
COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts ./
COPY src src
RUN chmod +x gradlew && ./gradlew installDist --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/build/install/ktor-stockmate .
EXPOSE 8080
ENTRYPOINT ["bin/ktor-stockmate"]
