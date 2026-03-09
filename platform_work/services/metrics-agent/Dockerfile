FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/metrics-agent-*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
