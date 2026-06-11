# ADR-004: Event Streaming using Kafka

## Status

Proposed

## Context

The Core Banking Middleware Modernization project aims to transform a monolithic middleware into a cloud-ready microservices architecture. A key aspect of this modernization is the adoption of an Event-Driven Architecture (EDA) to achieve loose coupling, real-time data processing, and enhanced scalability and resilience, as outlined in the `arc42_CBS_MIDDLEWARE_MODERNIZATION.md` document and further elaborated in ADR-003 (Microservice Communication Strategy).

The need for a robust, high-throughput, and fault-tolerant event streaming platform is critical for various use cases, including:
*   Propagating state changes across microservices.
*   Implementing event sourcing for critical business domains.
*   Integrating with external systems asynchronously.
*   Supporting real-time analytics and audit trails.

Traditional message queues often lack the scalability, durability, and stream processing capabilities required for these modern architectural patterns.

## Decision

We will adopt **Apache Kafka** as the primary event streaming platform for the Core Banking Middleware Modernization project.

## Consequences

### Positive

*   **High Throughput and Scalability**: Kafka is designed for high-volume, high-velocity data streams, capable of handling thousands of messages per second, which is essential for banking transactions. It scales horizontally by adding more brokers.
*   **Durability and Fault Tolerance**: Messages are persisted on disk and replicated across multiple brokers, ensuring data durability and fault tolerance even in the event of broker failures.
*   **Loose Coupling**: Services communicate indirectly via events, promoting strong decoupling. Producers don't need to know about consumers, and vice-versa, allowing independent evolution and deployment.
*   **Real-time Data Processing**: Enables real-time data pipelines and stream processing applications (e.g., using Kafka Streams or ksqlDB) for immediate insights and reactive systems.
*   **Event Sourcing Capabilities**: Kafka's immutable, ordered log nature makes it an excellent choice for implementing event sourcing patterns, providing a reliable source of truth for business events.
*   **Backpressure Handling**: Consumers pull messages at their own pace, effectively handling backpressure without overwhelming downstream services.
*   **Ecosystem Maturity**: Kafka has a vast and mature ecosystem of connectors, client libraries, and tools, simplifying integration with various data sources and sinks.
*   **Replayability**: The ability to replay historical events is crucial for debugging, testing, and building new services that need to process past data.

### Negative

*   **Operational Complexity**: Operating and managing a Kafka cluster (especially in production with high availability and disaster recovery) requires specialized knowledge and effort.
*   **Learning Curve**: Developers new to Kafka will need to understand concepts like topics, partitions, consumer groups, offsets, and delivery semantics.
*   **At-Least-Once Delivery**: Kafka guarantees at-least-once delivery, meaning consumers must be designed to be **idempotent** to handle potential duplicate messages. This adds complexity to consumer logic.
*   **Schema Management**: Evolving message schemas requires careful planning and the use of a schema registry (e.g., Confluent Schema Registry) to ensure compatibility between producers and consumers.
*   **Monitoring and Alerting**: Requires robust monitoring and alerting for broker health, consumer lag, and message throughput to ensure system stability.
*   **Data Retention**: Managing data retention policies for topics to control storage costs and compliance.

## Alternatives Considered

### 1. RabbitMQ (or other traditional Message Queues like ActiveMQ, IBM MQ)

*   **Pros**: Well-established, good for traditional message queuing patterns, often simpler to set up for basic use cases.
*   **Cons**: Not designed for high-throughput, durable event streaming or stream processing. Lacks the inherent scalability and replayability of Kafka. Less suitable for event sourcing or building real-time data pipelines.

### 2. AWS Kinesis / Azure Event Hubs / Google Cloud Pub/Sub

*   **Pros**: Fully managed services, reducing operational overhead. Offer similar capabilities to Kafka in a cloud-native context.
*   **Cons**: Vendor lock-in. May have different pricing models and feature sets that need careful evaluation against project requirements and existing infrastructure strategy. While managed, they still require understanding of streaming concepts. For an on-premise or hybrid cloud strategy, a self-managed or cloud-provider-agnostic solution like Kafka is often preferred.

### 3. Database-based Event Log

*   **Pros**: Simple to implement for very small scale, leverages existing database infrastructure.
*   **Cons**: Does not scale well for high throughput, lacks real-time notification capabilities, adds significant load to the database, and is not designed for stream processing. Not suitable for a large-scale middleware modernization.

Kafka was chosen due to its proven track record in high-performance, scalable, and durable event streaming, its rich ecosystem, and its strong alignment with the requirements of a modern, event-driven microservices architecture for core banking. The operational complexity is deemed manageable given the benefits and the availability of managed Kafka services or experienced DevOps teams.
