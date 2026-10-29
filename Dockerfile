# syntax=docker/dockerfile:1
# Layered image: fast rebuilds, small attack surface (JRE only at runtime).
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B -DskipTests package \
 && java -Djarmode=layertools -jar target/*.jar extract --destination target/extracted

FROM eclipse-temurin:22-jre-jammy AS runtime
RUN groupadd -r app && useradd -r -g app appuser
WORKDIR /app
COPY --from=build --chown=appuser:app /workspace/target/extracted/dependencies/ ./
COPY --from=build --chown=appuser:app /workspace/target/extracted/spring-boot-loader/ ./
COPY --from=build --chown=appuser:app /workspace/target/extracted/snapshot-dependencies/ ./
COPY --from=build --chown=appuser:app /workspace/target/extracted/application/ ./
USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "org.springframework.boot.loader.launch.JarLauncher"]
