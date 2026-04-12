FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY target/microservice-orders.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]