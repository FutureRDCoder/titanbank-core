# 🏦 TitanBank Core
### A Distributed, Bank-Grade Microservices Platform *(In Progress)*

---

## 📌 Overview

**TitanBank** is a distributed financial services platform designed to model how modern **investment banks and fintech companies** build **scalable, resilient, and compliant backend systems**.

This repository focuses on the **core backend services**, implemented using **Spring Boot microservices**, with a strong emphasis on:

- Clean architecture
- Domain separation
- Security-first design
- Production-ready coding practices

> ⚠️ **Note**  
> This project is under active development.  
> Only foundational services are implemented so far.

---

## 🎯 Project Goals

TitanBank aims to simulate **real-world banking infrastructure challenges**, including:

- Secure authentication & identity management
- Strong domain isolation via microservices
- Clear API contracts using DTOs
- Containerized local development
- Readiness for distributed systems patterns (Kafka, Saga, etc.)

This is **not a CRUD demo** — it is an **architecture-first system design project**.

---

## 🧱 Architecture (Current Scope)

```text

titanbank-core
│
├── services
│ ├── user-service → Authentication & user identity
│ ├── account-service → (Planned)
│ └── transaction-service → (Planned)
│
├── docker-compose.yml
└── pom.xml (multi-module)

```

---


### Architectural Style

- Microservices (Spring Boot)
- Maven multi-module setup
- RESTful APIs
- DTO-based request/response contracts
- Container-ready services

---

## 🔐 Implemented Service: User Service

### Responsibilities

- User registration
- User authentication
- Authenticated user profile access

### Key Design Principles

- Controllers are thin
- Business logic lives in the service layer
- No entities exposed via APIs
- Security-aware endpoint design

---

## 🌐 API Overview (User Service)

### Base URL

http://localhost:8081

---

### Authentication APIs

| Method | Endpoint | Description |
|------|---------|-------------|
| POST | `/api/v1/auth/register` | Register a new user |
| POST | `/api/v1/auth/login` | Authenticate user |

### User APIs (Authenticated)

| Method | Endpoint | Description |
|------|---------|-------------|
| GET | `/api/v1/users/me` | Get current authenticated user |

🔒 `/users/**` endpoints are designed to be protected via **Spring Security**.

---

### 📦 Example Request & Response

### 1. Register User

### a. Request

**POST** `/api/v1/auth/register`

```json
{
  "fullName": "Sundharam",
  "email": "user@titanbank.com",
  "password": "securePassword123"
}

```

### b. Response

```json
{
"accessToken": "jwt-access-token",
"refreshToken": "jwt-refresh-token"
}

```

---

### Tech Stack

| Layer            | Technology                      |
| ---------------- | ------------------------------- |
| Language         | Java 17                         |
| Framework        | Spring Boot                     |
| Build Tool       | Maven (Multi-Module)            |
| Containerization | Docker & Docker Compose         |
| API Style        | REST                            |
| Validation       | Jakarta Validation              |
| Security         | Spring Security *(in progress)* |

---

## Running Locally

### Prerequisites

```text

1. Java 17+

2. Maven 3.8+

3. Docker & Docker Compose

```

### Start Infrastructure

```text
docker-compose up -d
```

```text
Run User Service

cd services/user-service
./mvnw spring-boot:run

```

---

## 🧪 Current Development Status

| Component           | Status        |
| ------------------- | ------------- |
| User Service        | ✅ Implemented |
| Auth Controllers    | ✅ Implemented |
| DTO Discipline      | ✅ Enforced    |
| Security Config     | ⏳ In Progress |
| Account Service     | ⏳ Planned     |
| Transaction Service | ⏳ Planned     |
| Kafka / Saga        | ⏳ Planned     |
| Observability       | ⏳ Planned     |

---

## 📈 Why This Project Exists

TitanBank is built to:

1. Practice real backend architecture

2. Demonstrate system design thinking

3. Serve as a deep-dive portfolio project

4. Reflect how banks & fintechs actually build systems

5. This repository prioritizes correctness, clarity, and scalability over speed.


---

## 🚀 Next Planned Steps

1. Account Service design

2. Global exception handling

3. Spring Security + JWT end-to-end

4. Transaction orchestration (Saga pattern)

5. Kafka event streaming

6. Integration testing

7. CI/CD

## 👤 Author

Name:- Sundharam Dhanasekaran

Aspiring Backend & Distributed Systems Engineer
Focused on bank-grade systems, cloud, and scalability!

## 📜 License

This project is for educational and portfolio purposes.