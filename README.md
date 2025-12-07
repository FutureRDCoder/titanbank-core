# TitanBank - Investment Banking Platform

## Overview
Distributed microservices platform for trading, portfolio management, and financial transactions.

## Architecture
- **User Service** (Port 8081): Authentication, user management, KYC
- **Account Service** (Port 8082): Account management, balance operations
- **Transaction Service** (Port 8083): Money transfers, transaction processing

## Tech Stack
- Java 17
- Spring Boot 4.0.0
- PostgreSQL 15
- Redis 7
- Apache Kafka
- Maven (Multi-module)

## Prerequisites
- JDK 17+
- Maven 3.8+
- Docker & Docker Compose

## Getting Started

### 1. Start Infrastructure
```bash
docker-compose up -d
```

### 2. Build All Services
```bash
mvn clean install
```

### 3. Run Services
```bash
# Terminal 1
cd services/user-service
mvn spring-boot:run

# Terminal 2
cd services/account-service
mvn spring-boot:run

# Terminal 3
cd services/transaction-service
mvn spring-boot:run
```

## API Documentation
- User Service: http://localhost:8081/swagger-ui.html
- Account Service: http://localhost:8082/swagger-ui.html
- Transaction Service: http://localhost:8083/swagger-ui.html

## Project Status
🚧 **In Development** - Core banking services implementation

## Architecture Highlights
- Multi-module Maven structure for centralized dependency management
- Event-driven architecture with Kafka
- JWT-based authentication
- Redis caching for performance
- PostgreSQL with pessimistic locking for concurrent transactions
- Saga pattern for distributed transactions

## Author
Sundharam Dhanasekaran - Building TitanBank as a portfolio project to demonstrate distributed systems and microservices architecture.