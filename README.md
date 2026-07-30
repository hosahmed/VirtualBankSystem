# Virtual Bank System

A modern, microservices-based banking application built with **Spring Boot 3**, **Java 21**, **Apache Kafka**, and **MySQL**, orchestrated behind the **WSO2 API Manager** gateway. 

This project was developed as part of an internship to demonstrate advanced microservices architecture, inter-service communication, asynchronous event-driven logging, and secure API gateway management.

## 🏗️ Architecture

The system is composed of several decoupled microservices:

1. **User Service**: Manages user registration, authentication (login), and profile management.
2. **Account Service**: Handles the creation of bank accounts (Checking/Savings) and manages account balances. Includes strict validation against the User Service.
3. **Transaction Service**: Processes money transfers between accounts. Enforces transactional integrity and communicates synchronously with the Account Service via Spring `RestClient`.
4. **Logging Service**: An asynchronous Kafka Consumer that listens to the `logging` topic. It ingests audit logs (Requests/Responses) from all other microservices and persists them to a central log dump table.
5. **BFF Service (Backend for Frontend)**: Acts as an aggregator service for the frontend applications, orchestrating calls to multiple downstream microservices (e.g., fetching a unified user dashboard).
6. **WSO2 API Manager**: Acts as the single entry point (API Gateway) for all external traffic. Secures endpoints using OAuth2 Bearer tokens and enforces strict routing and mediation policies.

## 🛠️ Technology Stack

- **Backend Framework**: Spring Boot 3, Spring Data JPA, Spring Web, Spring Validation
- **Language**: Java 21
- **Database**: MySQL 8.0 (One database per microservice)
- **Message Broker**: Apache Kafka & Zookeeper (Confluent Platform)
- **API Gateway**: WSO2 API Manager 4.x
- **Containerization**: Docker & Docker Compose
- **Documentation**: Swagger / OpenAPI 3 (Springdoc)
- **Build Tool**: Maven

## 🚀 Getting Started

### Prerequisites
- [Docker & Docker Compose](https://www.docker.com/)
- [Java 21](https://adoptium.net/) & [Maven](https://maven.apache.org/) (for local development)
- [WSO2 API Manager 4.x](https://wso2.com/api-manager/) installed locally

### Running the Microservices

The entire infrastructure (Databases, Kafka, Zookeeper, and the Spring Boot Microservices) is fully containerized.

1. Open a terminal in the root directory of the project.
2. Run the following command to spin up the entire cluster:
   ```bash
   docker-compose up -d --build
   ```
3. Wait a few moments for Kafka and the MySQL databases to initialize before the microservices fully start.

### Ports Overview
When running locally via Docker, the services map to the following ports:
- **User Service**: `http://localhost:8081`
- **Account Service**: `http://localhost:8082`
- **Transaction Service**: `http://localhost:8083`
- **BFF Service**: `http://localhost:8084`
- **Logging Service**: `http://localhost:8085`
- **WSO2 Gateway**: `https://localhost:8243`

## 🛡️ WSO2 API Manager Setup

To properly secure the application, you must import the APIs into WSO2 and set up the Gateway:

1. Start your local WSO2 API Manager instance (`api-manager.bat` / `api-manager.sh`).
2. Log into the **Publisher Portal** (`https://localhost:9443/publisher`).
3. Import the OpenAPI definitions from the running microservices (e.g., `http://localhost:8081/v3/api-docs`).
4. **Important**: Delete internal endpoints from the public APIs to maintain architectural security (e.g., remove the profile fetch endpoint from the public `RegisterAPI`).
5. Create the `vbank` **API Product** to group the exposed endpoints together.
6. Log into the **Developer Portal**, subscribe to the `vbank` product, and generate your OAuth Access Token.

## 🧪 Testing

You can test the application natively through **Postman**:
1. Import the `.json` file into Postman.
2. Configure your Bearer Token in the Authorization tab.

