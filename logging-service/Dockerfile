# Build stage
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app

# Copy the parent pom
COPY pom.xml .

# Copy the logging-service source and pom
COPY logging-service/pom.xml logging-service/
COPY logging-service/src logging-service/src/

# Package the application
RUN mvn -B -f logging-service/pom.xml clean package -DskipTests

# Run stage
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=builder /app/logging-service/target/logging-service.jar app.jar

EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]
