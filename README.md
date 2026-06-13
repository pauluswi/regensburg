# Core Banking Middleware Modernization Showcase

![Status](https://img.shields.io/badge/Status-Showcase-blue)
![Language](https://img.shields.io/badge/Language-Java_21-orange?logo=java&logoColor=white)
![Framework](https://img.shields.io/badge/Framework-Spring_Boot-6DB33F?logo=springboot&logoColor=white)
![Orchestration](https://img.shields.io/badge/Orchestration-Kubernetes-326CE5?logo=kubernetes&logoColor=white)
![Eventing](https://img.shields.io/badge/Eventing-Kafka-231F20?logo=apachekafka&logoColor=white)
![Cache/Idempotency](https://img.shields.io/badge/Cache/Idempotency-Redis-DC382D?logo=redis&logoColor=white)
![CI/CD](https://img.shields.io/badge/CI/CD-GitHub_Actions-2088FF?logo=githubactions&logoColor=white)
![Architecture](https://img.shields.io/badge/Architecture-arc42%20%2F%20ADRs-purple)

## Project Overview

This repository serves as a comprehensive showcase for the architectural decisions and implementation patterns involved in a **Core Banking Middleware Modernization** project. The primary goal was to transform a monolithic middleware into a cloud-native, microservices-based architecture, enhancing resilience, scalability, and maintainability for banking integrations.

This project demonstrates the strategic approach taken to:
*   Architect the migration of banking middleware from monolithic systems to cloud-native microservices.
*   Design integration services for core banking and external financial institutions using ISO 8583 and REST APIs.
*   Implement Kubernetes-based deployment and resiliency practices to enhance scalability and operational stability.
*   Lay the groundwork for reducing system downtime and improving the speed of delivering new banking capabilities.

## Architectural Documentation

The architectural decisions are meticulously documented using the arc42 framework and Architecture Decision Records (ADRs), providing a clear rationale for each significant choice.

### arc42 Architecture Document
*   **[arc42 – Core Banking Middleware Modernization](docs/architecture/arc42_CBS_MIDDLEWARE_MODERNIZATION.md)**: The main architecture document detailing the context, solution strategy, building blocks, runtime, deployment, cross-cutting concepts, and quality requirements.

### Architecture Decision Records (ADRs)
A series of ADRs capture specific architectural decisions, their context, consequences, and alternatives considered:
*   **[ADR-001: Java Version 17 vs 21](docs/architecture/adr-001-java-version-17-vs-21.md)**
*   **[ADR-002: API Design Guidelines and Versioning](docs/architecture/adr-002-api-design-guidelines-and-versioning.md)**
*   **[ADR-003: Microservice Communication Strategy (Synchronous vs. Asynchronous)](docs/architecture/adr-003-microservice-communication-strategy.md)**
*   **[ADR-004: Event Streaming using Kafka](docs/architecture/adr-004-event-streaming-using-kafka.md)**
*   **[ADR-005: Redis for Idempotency and Caching](docs/architecture/adr-005-redis-for-idempotency-and-caching.md)**
*   **[ADR-006: Database Strategy for Microservices](docs/architecture/adr-006-database-strategy-for-microservices.md)**
*   **[ADR-007: Authentication and Authorization Mechanism](docs/architecture/adr-007-authentication-and-authorization-mechanism.md)**
*   **[ADR-008: Error Handling and Resilience Patterns](docs/architecture/adr-008-error-handling-and-resilience-patterns.md)**
*   **[ADR-009: Centralized Logging, Monitoring, and Tracing (Observability Stack)](docs/architecture/adr-009-centralized-observability-stack.md)**
*   **[ADR-010: Containerization and Deployment Strategy](docs/architecture/adr-010-containerization-and-deployment-strategy.md)**
*   **[ADR-011: Data Migration Strategy from Legacy Systems](docs/architecture/adr-011-data-migration-strategy-from-legacy-systems.md)**
*   **[ADR-012: API Gateway Product Selection and Responsibilities](docs/architecture/adr-012-api-gateway-product-selection-and-responsibilities.md)**
*   **[ADR-013: Configuration Management Strategy](docs/architecture/adr-013-configuration-management-strategy.md)**
*   **[ADR-014: Testing Strategy for Microservices](docs/architecture/adr-014-testing-strategy-for-microservices.md)**
*   **[ADR-015: Migration Strategy (Deep Dive into Strangler Fig)](docs/architecture/adr-015-migration-strategy-deep-dive-into-strangler-fig.md)**
*   **[ADR-016: Integration Adapter Design (for ISO 8583/20022)](docs/architecture/adr-016-integration-adapter-design-iso8583-20022.md)**
*   **[ADR-017: CI/CD Pipeline Architecture](docs/architecture/adr-0017-ci-cd-pipeline-architecture.md)**

### Architectural Styles and Patterns
*   **[Architectural Styles and Patterns](docs/architecture/architectural_styles_and_patterns.md)**: A foundational document explaining the theoretical underpinnings of the chosen architectural styles and patterns.

### Trade-offs Summary
*   **[Architectural Decisions: Trade-offs Summary](docs/architecture/trade-off.md)**: A concise overview of the key decisions, chosen options, and alternatives considered across all ADRs.

## Showcase Artifacts

To provide concrete examples of the architectural decisions in practice, the repository includes:

### 1. Mock Code Snippets for a Sample Microservice
A `payment-service` microservice (`src/main/java/com/regensburg/paymentservice/mock`) demonstrating:
*   **REST API**: `PaymentController` exposing `/api/v1/payments`.
*   **Idempotency**: `RedisIdempotencyService` for handling duplicate requests.
*   **Resilience**: `ExternalFraudDetectionService` protected by a Circuit Breaker.
*   **Event Streaming**: `KafkaProducerService` for publishing `PaymentEvent`s.
*   **Unit Tests**: `PaymentServiceTest` (`src/test/java/com/regensburg/paymentservice/mock/service/PaymentServiceTest.java`) validating the core logic.

### 2. Example Kubernetes Manifests
Declarative YAML files (`k8s-manifests/`) for deploying the `payment-service` to Kubernetes, showcasing:
*   `payment-service-deployment.yaml`
*   `payment-service-service.yaml`
*   `payment-service-configmap.yaml`
*   `payment-service-secret.yaml` (placeholder for Vault integration)
*   `payment-service-ingress.yaml`

### 3. Simplified CI/CD Pipeline Definition
A GitHub Actions workflow (`.github/workflows/ci-cd-pipeline.yaml`) outlining stages for:
*   Build & Unit Test
*   Build & Push Docker Image
*   Integration & Contract Tests
*   Deploy to Dev Environment

### 4. OpenAPI Specification Snippet
An OpenAPI (Swagger) definition (`docs/architecture/api-docs/payment-service-openapi.yaml`) for the `payment-service` API, reinforcing API design guidelines.

### 5. Visual C4 Model Diagrams
PlantUML definitions (`docs/architecture/diagrams/`) for:
*   **[C4 Context Diagram](docs/architecture/diagrams/c4_context_diagram.puml)**
*   **[C4 Container Diagram](docs/architecture/diagrams/c4_container_diagram.puml)**

## How to Explore

1.  **Review Architectural Documentation**: Start with the [arc42 document](docs/architecture/arc42_CBS_MIDDLEWARE_MODERNIZATION.md) for a high-level overview, then dive into specific [ADRs](docs/architecture/) for detailed decisions. The [Trade-offs Summary](docs/architecture/trade-off.md) provides a quick glance at key choices.
2.  **Examine Code Snippets**: Explore the `src/main/java/com/regensburg/paymentservice/mock` directory to see how architectural patterns are implemented in Java.
3.  **Run Unit Tests**: Navigate to `src/test/java/com/regensburg/paymentservice/mock/service/PaymentServiceTest.java` and run the tests to see the service's behavior validated.
4.  **View Kubernetes Manifests**: Inspect the `k8s-manifests/` directory to understand the deployment strategy.
5.  **Analyze CI/CD Pipeline**: Review `.github/workflows/ci-cd-pipeline.yaml` to see the automated delivery process.
6.  **Check API Documentation**: Open `docs/architecture/api-docs/payment-service-openapi.yaml` (ideally with a Swagger UI tool) to visualize the API contract.
7.  **Render C4 Diagrams**: Use an IDE plugin or an online PlantUML renderer to visualize the `.puml` files in `docs/architecture/diagrams/`.
