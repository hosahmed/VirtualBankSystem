# Build stage
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app

# Copy the parent pom
COPY pom.xml .

# Copy the transaction-service source and pom
COPY transaction-service/pom.xml transaction-service/
COPY transaction-service/src transaction-service/src/

# Package the application
RUN mvn -B -f transaction-service/pom.xml clean package -DskipTests

# Run stage
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=builder /app/transaction-service/target/transaction-service.jar app.jar

EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
