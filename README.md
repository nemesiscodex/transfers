# Reliable API Calls - Implementation Examples

A Spring Boot 4 demonstration project showcasing three approaches to handling third-party API calls with increasing levels of reliability and scalability.

This project implements the patterns described in the blog post: [**These 3 Patterns Will Make Your 3rd-Party API Calls More Reliable**](https://blog.nemesiscodex.org/reliable-api-calls/)

## 🎯 Overview

This repository contains three implementations of a transfer service that integrates with a third-party banking API:

1. **Naive Approach** (`naive/`) - The basic implementation that demonstrates common pitfalls
2. **Improved Approach** (`improved/`) - Implements the 3 reliability patterns:
   - **Idempotency Keys** - Prevent duplicate processing
   - **State Machines** - Track transaction lifecycle
   - **Optimistic Locking** - Prevent race conditions
3. **Scalable Approach** (`scalable/`) - Asynchronous processing with message queues (coming soon)

## 🛠️ Tech Stack

- **Java 25** - Latest JDK features
- **Spring Boot 4.0.0-SNAPSHOT** - Reactive Spring framework
- **Spring WebFlux** - Reactive web layer
- **R2DBC** - Reactive database connectivity
- **PostgreSQL** - Primary database
- **Valkey (Redis-compatible)** - Caching and session management
- **Kafka** (via Spring Cloud Stream) - Message queue for async processing
- **Flyway** - Database migrations
- **JWT** - Stateless authentication
- **Testcontainers** - Integration testing

## 📋 Prerequisites

- **Java 25** (see `.sdkmanrc` for version management)
- **Docker** and **Docker Compose** (for local infrastructure)
- **Gradle** (wrapper included)

## 🚀 Quick Start

### 1. Setup Java Version

If using SDKMAN, activate the correct Java version from `.sdkmanrc`:

```bash
sdk env
```

### 2. Start Infrastructure

Start PostgreSQL and Valkey (Redis-compatible) using Docker Compose:

```bash
docker compose up -d
```

This will start:
- PostgreSQL on port `5432`
- Valkey on port `6379`

### 3. Configure Application

The application uses default configuration in `src/main/resources/application.yaml`. For production, override these values via environment variables:

```bash
export JWT_SECRET=your-secret-key-at-least-256-bits
export SPRING_R2DBC_URL=r2dbc:postgresql://localhost:5432/transfers
export SPRING_R2DBC_USERNAME=postgres
export SPRING_R2DBC_PASSWORD=postgres
```

### 4. Run the Application

```bash
./gradlew bootRun
```

Flyway will automatically run migrations on startup. The application will be available at `http://localhost:8080`.

### 5. Run Tests

```bash
./gradlew test
```

Tests use Testcontainers and will automatically start required containers. The test profile is automatically activated.

## 📁 Project Structure

```
src/main/java/org/nemesiscodex/transfers/
├── core/                    # Core domain (users, auth, security)
│   ├── controller/         # REST controllers
│   ├── service/            # Business logic
│   ├── repository/         # R2DBC repositories
│   ├── entity/             # Domain entities
│   ├── dto/                # Data transfer objects
│   └── security/           # Security configuration
├── naive/                   # Naive implementation (demonstrates pitfalls)
├── improved/                # Improved implementation (3 reliability patterns)
├── scalable/                # Scalable implementation (async processing)
└── config/                  # Spring configuration

src/main/resources/
├── application.yaml         # Application configuration
└── db/migration/           # Flyway migrations
```

## 🔑 Key Concepts

### Pattern 1: Idempotency Keys

Each request includes a unique `Idempotency-Key` header. The server uses this key to:
- Detect duplicate requests
- Return cached results for retries
- Prevent duplicate processing

### Pattern 2: State Machines

Transactions move through explicit states:
- `PENDING` → Initial state after creation
- `PROCESSING` → API call in progress
- `COMPLETED` → Successfully processed
- `FAILED` → Processing failed

### Pattern 3: Optimistic Locking

State transitions use conditional updates:
```sql
UPDATE transactions 
SET state = 'COMPLETED', result = ?
WHERE idempotency_key = ? AND state = 'PROCESSING'
```

This ensures atomic state transitions and prevents race conditions.

## 🧪 Testing

The project uses:
- **JUnit 5** - Test framework
- **Spring Boot Test** - Integration testing
- **Testcontainers** - Containerized dependencies
- **Reactor StepVerifier** - Reactive stream testing

Run specific test classes:
```bash
./gradlew test --tests 'org.nemesiscodex.transfers.naive.*'
./gradlew test --tests 'org.nemesiscodex.transfers.improved.*'
```

## 📚 Related Resources

- [Blog Post: These 3 Patterns Will Make Your 3rd-Party API Calls More Reliable](https://blog.nemesiscodex.org/reliable-api-calls/)
- [Spring WebFlux Documentation](https://docs.spring.io/spring-framework/reference/web/webflux.html)
- [R2DBC Documentation](https://r2dbc.io/)

## 🤝 Contributing

This is a demonstration project. Feel free to:
- Report issues
- Suggest improvements
- Submit pull requests

## 📝 License

This project is provided as-is for educational purposes.

---

**Note**: This project demonstrates patterns for reliable API integrations. Always consider your specific requirements and constraints when implementing these patterns in production systems.

