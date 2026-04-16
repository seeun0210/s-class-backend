FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Cache Gradle dependencies
COPY gradlew .
COPY gradle/ gradle/
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY SClass-Common/build.gradle.kts SClass-Common/
COPY SClass-Domain/build.gradle.kts SClass-Domain/
COPY SClass-Infrastructure/build.gradle.kts SClass-Infrastructure/
COPY SClass-Api-Supporters/build.gradle.kts SClass-Api-Supporters/
COPY SClass-Api-Backoffice/build.gradle.kts SClass-Api-Backoffice/
COPY SClass-Batch/build.gradle.kts SClass-Batch/
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon || true

# Copy source and build
COPY . .
# application.yml is gitignored — use .example files (env var placeholders only, no secrets)
RUN find . -name "application.yml.example" -exec sh -c 'cp "$1" "${1%.example}"' _ {} \;
ARG MODULE
RUN ./gradlew :${MODULE}:bootJar -x test --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
ARG MODULE
COPY --from=builder /app/${MODULE}/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
