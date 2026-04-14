FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY target/microservice-orders.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]