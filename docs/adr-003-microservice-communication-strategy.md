# ADR-003: Microservice Communication Strategy (Synchronous vs. Asynchronous)

## Status

Proposed

## Context

In a microservices architecture, effective communication between services is paramount for the system's overall functionality, performance, and resilience. The Core Banking Middleware Modernization project involves numerous microservices that need to interact with each other and with external systems. Choosing the right communication paradigm (synchronous or asynchronous) for different scenarios is critical to ensure loose coupling, scalability, fault tolerance, and responsiveness. Without a clear strategy, services might become tightly coupled, leading to cascading failures, performance bottlenecks, and difficulties in maintenance and evolution.

## Decision

We will adopt a hybrid communication strategy, clearly defining when to use synchronous and asynchronous communication patterns based on specific use cases and requirements.

### 1. Synchronous Communication

**Purpose**: Used for request-response interactions where immediate feedback is required, and the calling service needs to wait for a direct response from the called service.

**Technology**: Primarily **RESTful HTTP/1.1 or HTTP/2 APIs**. gRPC may be considered for high-performance internal service-to-service communication where strict schema enforcement and lower latency are critical.

**Use Cases**:
*   **Direct Query/Retrieval**: Fetching current state or data (e.g., `GET /accounts/{accountId}`).
*   **Immediate Action with Direct Result**: Operations where the client needs to know the immediate success or failure (e.g., `POST /transactions` for a simple, fast transaction).
*   **API Gateway to Backend Services**: Initial routing and aggregation of requests from external clients.

**Considerations**:
*   **Resilience**:
    *   **Timeouts**: Implement aggressive connection and read timeouts to prevent services from hanging indefinitely.
    *   **Circuit Breakers**: Use circuit breakers (e.g., Resilience4j) to prevent cascading failures when a downstream service is unhealthy.
    *   **Retries**: Implement idempotent retries with exponential backoff for transient network issues or temporary service unavailability.
*   **Error Handling**: Utilize standard HTTP status codes and a consistent error response format (as defined in ADR-002).
*   **Load Balancing**: Rely on service mesh (e.g., Istio) or client-side load balancing for distributing requests across service instances.
*   **Security**: Enforce mutual TLS (mTLS) for internal service-to-service communication and OAuth2/OIDC for external API access.

### 2. Asynchronous Communication

**Purpose**: Used for event-driven interactions, decoupling services, long-running processes, high-throughput data streams, and scenarios where immediate response is not critical, or eventual consistency is acceptable.

**Technology**: Primarily **Apache Kafka** for event streaming and message queuing.

**Use Cases**:
*   **Event Sourcing**: Capturing all changes to application state as a sequence of events.
*   **Command Queuing**: Decoupling the command issuer from the command processor for long-running or resource-intensive tasks.
*   **Data Replication/Synchronization**: Propagating data changes across multiple services or data stores.
*   **Notifications**: Sending notifications to various consumers without direct coupling.
*   **Audit Logging**: Asynchronously logging events for auditing and compliance.
*   **Saga Orchestration**: Coordinating distributed transactions across multiple services (as mentioned in `arc42`).

**Considerations**:
*   **Idempotency**: Consumers of asynchronous messages **must** be designed to be idempotent to handle potential duplicate message delivery (at-least-once delivery guarantee of Kafka).
*   **Guaranteed Delivery**: Configure Kafka producers and consumers for at-least-once delivery to prevent message loss.
*   **Message Ordering**: Utilize Kafka topic partitioning and consumer groups to ensure message ordering within a partition where required.
*   **Error Handling**:
    *   **Dead Letter Queues (DLQ)**: Implement DLQs for messages that cannot be processed successfully after a defined number of retries.
    *   **Monitoring**: Monitor consumer lag and error rates to detect processing issues.
    *   **Observability**: Ensure correlation IDs are propagated through asynchronous message headers for end-to-end tracing.
*   **Schema Evolution**: Use a schema registry (e.g., Confluent Schema Registry) with Avro or Protobuf to manage message schema evolution and ensure backward/forward compatibility.

## Consequences

### Positive

*   **Increased Decoupling**: Services become more independent, reducing dependencies and allowing for independent deployment and scaling.
*   **Enhanced Resilience**: Asynchronous communication inherently handles failures better by queuing messages, preventing cascading failures, and allowing services to recover independently.
*   **Improved Scalability**: Message queues and event streams can buffer high loads, allowing services to process messages at their own pace and scale independently.
*   **Better Responsiveness**: Clients can receive immediate acknowledgments for requests, even if the actual processing is long-running and asynchronous.
*   **Facilitates Event-Driven Architectures**: Enables complex business processes to be modeled as sequences of events, leading to more flexible and reactive systems.

### Negative

*   **Increased Complexity**: Distributed asynchronous systems are harder to design, implement, test, and debug due to eventual consistency, message ordering, and error handling challenges.
*   **Eventual Consistency**: Data consistency is not immediate, which requires careful design and understanding by developers.
*   **Operational Overhead**: Managing and monitoring message brokers (like Kafka) adds operational complexity.
*   **Debugging Challenges**: Tracing a request through multiple asynchronous hops can be more difficult without robust observability tools.

## Alternatives Considered

### 1. Purely Synchronous Communication

*   **Pros**: Simpler to understand and debug initially for simple request-response flows.
*   **Cons**: Leads to tight coupling, cascading failures, poor scalability under high load, and difficulty in handling long-running processes. Not suitable for a modern, resilient microservices architecture.

### 2. Purely Asynchronous Communication

*   **Pros**: Maximum decoupling, high resilience, and scalability.
*   **Cons**: Not suitable for all immediate request-response needs where clients expect instant feedback. Can over-engineer simple interactions and complicate debugging for straightforward flows.

### 3. Enterprise Service Bus (ESB)

*   **Pros**: Centralized control over integrations, often provides rich transformation and routing capabilities.
*   **Cons**: Can become a single point of failure and a bottleneck, leading to tight coupling to the ESB itself. Often complex and expensive to maintain, contradicting the microservices philosophy of decentralized governance. This approach is explicitly moved away from in the modernization effort.
