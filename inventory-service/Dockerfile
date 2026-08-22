FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn dependency:go-offline -B
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn package -DskipTests -B
RUN java -Djarmode=layertools -jar target/*.jar extract --destination extracted

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -g 1001 -S appgroup && adduser -u 1001 -S appuser -G appgroup
WORKDIR /app
COPY --from=builder /app/extracted/dependencies/ ./
COPY --from=builder /app/extracted/spring-boot-loader/ ./
COPY --from=builder /app/extracted/snapshot-dependencies/ ./
COPY --from=builder /app/extracted/application/ ./
HEALTHCHECK --interval=30s --timeout=5s --retries=3 --start-period=30s \
    CMD wget -qO- http://localhost:8083/actuator/health || exit 1
USER appuser
EXPOSE 8083
ENTRYPOINT ["java", "-XX:+UseZGC", "-Xms256m", "-Xmx1g", \
    "-XX:MaxRAMPercentage=75.0", "org.springframework.boot.loader.launch.JarLauncher"]