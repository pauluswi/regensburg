# ADR-012: API Gateway Product Selection and Responsibilities

## Status

Proposed

## Context

The Core Banking Middleware Modernization project relies heavily on APIs as the primary interface for external clients (Mobile Banking, Internet Banking, partners) and potentially for internal service-to-service communication. As the system grows with numerous microservices, a central entry point is crucial to manage cross-cutting concerns, provide a unified API experience, and protect backend services.

While ADR-002 (API Design Guidelines and Versioning) defines how APIs should be designed, this ADR focuses on the infrastructure component responsible for exposing and managing these APIs. Without a dedicated API Gateway, each microservice would need to handle concerns like authentication, rate limiting, and routing, leading to duplication of effort, inconsistent implementation, and increased complexity for clients.

The API Gateway acts as the "front door" to the microservices ecosystem, handling requests before they reach individual services.

## Decision

We will adopt **Spring Cloud Gateway** as the primary API Gateway for the Core Banking Middleware Modernization project. For advanced traffic management and service-to-service security within the Kubernetes cluster, **Istio Gateway** will be considered as an additional layer or alternative if a full service mesh (Istio) is adopted.

### 1. Selected API Gateway: Spring Cloud Gateway

*   **Rationale**:
    *   **Native Spring Ecosystem Integration**: Seamless integration with Spring Boot microservices, leveraging familiar programming models and configurations.
    *   **Lightweight and Performant**: Built on Spring WebFlux (reactive programming), offering non-blocking I/O and high performance.
    *   **Extensibility**: Highly customizable with filters and predicates, allowing for custom logic implementation.
    *   **Kubernetes Friendly**: Easily deployable and manageable within a Kubernetes environment.
    *   **Cost-Effective**: Open-source, avoiding licensing costs associated with commercial products.

### 2. Responsibilities of the API Gateway

The API Gateway will be responsible for the following cross-cutting concerns:

*   **Request Routing**: Directing incoming requests to the appropriate backend microservice based on defined routes (e.g., path, host, headers).
*   **Authentication Offloading**: Validating incoming JWTs (as per ADR-007) and ensuring that only authenticated requests reach backend services. This offloads authentication logic from individual microservices.
*   **Authorization Enforcement (Coarse-Grained)**: Performing initial, coarse-grained authorization checks (e.g., based on scopes or roles in the JWT) before forwarding requests. Fine-grained authorization will remain within the microservices.
*   **Rate Limiting**: Protecting backend services from being overwhelmed by excessive requests by applying rate limits per client, API, or user.
*   **Load Balancing**: Distributing requests across multiple instances of a backend microservice.
*   **Circuit Breaker Integration**: Integrating with resilience patterns (as per ADR-008) to prevent cascading failures when backend services are unhealthy.
*   **Request/Response Transformation**: Modifying request headers, body, or response headers/body as needed (e.g., adding correlation IDs, removing sensitive information).
*   **API Composition/Aggregation (Limited)**: For simple cases, the Gateway might aggregate responses from multiple backend services. For complex compositions, dedicated aggregation services will be preferred.
*   **SSL/TLS Termination**: Handling encrypted traffic from clients, decrypting it, and forwarding it to backend services (potentially re-encrypting with mTLS for internal communication).
*   **Observability Integration**: Injecting correlation IDs (as per ADR-009) and collecting metrics and logs for requests passing through it.

### 3. Role of Istio Gateway (Conditional)

If a full **Istio Service Mesh** is adopted across the Kubernetes cluster, the Istio Gateway (Ingress Gateway) would serve as the primary entry point for external traffic. In this scenario:
*   **Istio Gateway** would handle L4-L7 traffic management, mTLS, and potentially some authentication/authorization.
*   **Spring Cloud Gateway** could still be deployed *behind* the Istio Gateway to provide more application-specific routing, request/response transformations, and business logic-driven API management, acting as a "BFF" (Backend for Frontend) or a more specialized API management layer.
*   The decision to fully adopt Istio and its Gateway will be subject to a separate ADR.

## Consequences

### Positive

*   **Centralized Control**: Provides a single point for managing cross-cutting concerns, ensuring consistency and reducing duplication.
*   **Enhanced Security**: Centralized authentication and authorization enforcement, protecting backend services.
*   **Simplified Client Interaction**: Clients interact with a single, stable API endpoint, abstracting away backend service complexity.
*   **Improved Traffic Management**: Enables dynamic routing, load balancing, and resilience patterns.
*   **Increased Agility**: Allows backend services to evolve independently without impacting clients, as long as the API Gateway contract is maintained.
*   **Better Observability**: Centralized point for collecting metrics, logs, and traces for all incoming traffic.

### Negative

*   **Single Point of Failure**: If not deployed with high availability, the API Gateway can become a single point of failure for the entire system.
*   **Performance Bottleneck**: The Gateway can become a performance bottleneck if not properly scaled and optimized.
*   **Increased Latency**: Adds an additional hop to every request, introducing a small amount of latency.
*   **Operational Complexity**: Requires careful configuration, monitoring, and management.
*   **Over-Centralization Risk**: Overloading the Gateway with too much business logic can turn it into a new monolith.

## Alternatives Considered

### 1. No API Gateway (Direct Service Exposure)

*   **Pros**: Simplest setup initially.
*   **Cons**: Each microservice must handle its own security, rate limiting, etc., leading to duplication and inconsistency. Clients need to know the addresses of all services. Highly insecure and unmanageable for a large microservices landscape. Rejected.

### 2. Commercial API Gateway Products (e.g., Kong, Apigee, AWS API Gateway)

*   **Pros**: Feature-rich, often include advanced analytics, developer portals, and enterprise support.
*   **Cons**: High licensing costs (for commercial products), potential vendor lock-in, may not integrate as seamlessly with a Spring Boot ecosystem, can be overkill for initial requirements. Kong was considered for its open-source nature and plugin ecosystem but Spring Cloud Gateway's native Spring integration was preferred.

### 3. Nginx/HAProxy as a Reverse Proxy

*   **Pros**: High performance, well-understood, good for basic routing and load balancing.
*   **Cons**: Lacks advanced API management features (authentication offloading, rate limiting, dynamic routing based on service discovery) out-of-the-box, requiring significant custom configuration and scripting. Not designed for the dynamic nature of microservices.

Spring Cloud Gateway is chosen for its strong alignment with the project's technology stack, its flexibility, and its ability to handle the required API management responsibilities efficiently within a Kubernetes environment.
