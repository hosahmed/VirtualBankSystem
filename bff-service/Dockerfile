# Build stage
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app

# Copy the parent pom
COPY pom.xml .

# Copy the bff-service source and pom
COPY bff-service/pom.xml bff-service/
COPY bff-service/src bff-service/src/

# Package the application
RUN mvn -B -f bff-service/pom.xml clean package -DskipTests

# Run stage
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=builder /app/bff-service/target/bff-service.jar app.jar

EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
