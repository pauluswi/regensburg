# ADR-006: Database Strategy for Microservices

## Status

Proposed

## Context

The Core Banking Middleware Modernization project adopts a microservices architecture to achieve greater agility, scalability, and resilience. A fundamental principle of microservices is the independent deployability and autonomy of each service. This autonomy extends to data persistence, where each microservice should ideally manage its own data store.

Without a clear strategy for data persistence, there is a risk of creating tightly coupled services through shared databases, leading to:
*   Reduced service autonomy.
*   Increased deployment risks (schema changes affecting multiple services).
*   Difficulty in scaling individual services.
*   Technology lock-in for the entire system.

This ADR defines the guidelines for managing data persistence for individual microservices, adhering to the "database per service" principle.

## Decision

We will implement a **"Database per Service"** strategy, where each microservice owns its private database. This decision includes guidelines for choosing database types, managing data ownership, handling schema evolution, and addressing data synchronization needs.

### 1. Database per Service Principle

*   **Dedicated Data Store**: Each microservice will have its own dedicated data store (database, schema, or even a separate table space, depending on the database technology and organizational constraints).
*   **Exclusive Access**: Only the owning microservice is permitted to directly access its database. Other services must interact with the data solely through the owning service's public API or by consuming events published by the owning service.
*   **Technology Diversity**: Services are free to choose the most appropriate database technology (relational, NoSQL, etc.) for their specific needs, provided it aligns with approved technologies and operational capabilities.

### 2. Choosing Database Types

*   **Relational Databases (e.g., PostgreSQL, MySQL)**:
    *   **Use Cases**: Preferred for services requiring strong ACID compliance, complex transactional integrity, mature querying capabilities (SQL), and well-defined relational data models.
    *   **Considerations**: Suitable for core banking entities, financial transactions, and other critical business data where consistency is paramount.
*   **NoSQL Databases (e.g., MongoDB, Cassandra, Redis for specific use cases)**:
    *   **Use Cases**: Considered for services requiring high scalability, flexible schema, high-throughput writes, or specific data access patterns (e.g., document storage, key-value lookups, graph data).
    *   **Considerations**: Suitable for audit logs, user profiles, session management, real-time analytics data, or other data where eventual consistency is acceptable and the data model is less rigid.
*   **In-Memory Data Stores (e.g., Redis)**:
    *   **Use Cases**: Primarily for caching, session management, rate limiting, and idempotency (as defined in ADR-005). Not typically used as a primary persistent data store for core business data.

### 3. Data Ownership and Schema Evolution

*   **Clear Ownership**: The team responsible for a microservice is solely responsible for its database schema, data integrity, and evolution.
*   **Independent Schema Evolution**: Schema changes within a service's database can be made independently without affecting other services, as long as the service's public API remains backward compatible (as per ADR-002).
*   **Migration Strategies**: Database schema migrations should be automated and integrated into the service's deployment pipeline (e.g., using Flyway or Liquibase).

### 4. Data Synchronization and Integration

*   **Avoid Direct Database Access**: Services must never directly access another service's database.
*   **API Calls**: For services requiring data from another service, they should consume the owning service's public API (synchronous communication, as per ADR-003).
*   **Event-Driven Data Propagation**: For data that needs to be shared or replicated across multiple services, the owning service will publish relevant domain events to Apache Kafka (as per ADR-004). Other services can then subscribe to these events and maintain their own denormalized copies or materialized views of the data. This promotes eventual consistency.
*   **Materialized Views**: Services may create and maintain their own materialized views of data owned by other services, populated via event streams, to optimize their read performance and reduce cross-service API calls.

## Consequences

### Positive

*   **Service Autonomy**: Each service can evolve its data model and persistence technology independently, fostering faster development and deployment cycles.
*   **Improved Scalability**: Services can scale their databases independently based on their specific load patterns.
*   **Technology Diversity**: Teams can choose the best-fit database technology for each service's unique requirements.
*   **Enhanced Resilience**: A database failure in one service is less likely to directly impact other services.
*   **Clearer Boundaries**: Enforces strong service boundaries and reduces implicit coupling.

### Negative

*   **Data Consistency Challenges**: Achieving strong transactional consistency across multiple services becomes more complex, often requiring eventual consistency patterns (e.g., Sagas, Outbox Pattern).
*   **Increased Operational Complexity**: Managing multiple database instances and potentially diverse database technologies adds to operational overhead (monitoring, backups, patching).
*   **Data Duplication**: Data may be duplicated across services (e.g., materialized views), requiring careful management to ensure consistency and avoid stale data.
*   **Complex Queries**: Queries that span data owned by multiple services become more complex, often requiring API composition, event-driven materialized views, or dedicated reporting services.
*   **Higher Resource Consumption**: Running multiple database instances can consume more resources than a single monolithic database.

## Alternatives Considered

### 1. Shared Database

*   **Description**: Multiple microservices share a single database instance or schema.
*   **Pros**: Simpler to manage initially, easier to achieve ACID transactions across services.
*   **Cons**: Creates tight coupling between services, reduces autonomy, makes independent deployment difficult, leads to schema contention, and hinders independent scaling. This is an anti-pattern in microservices and is explicitly rejected.

### 2. Monolithic Database

*   **Description**: A single, large database serving the entire application, typical of monolithic architectures.
*   **Pros**: Centralized data management, strong consistency, simpler queries.
*   **Cons**: Becomes a bottleneck, difficult to scale, technology lock-in, hinders independent development and deployment of application components. This is the legacy approach the modernization project aims to move away from.

The "Database per Service" pattern, despite its complexities, is chosen as it aligns best with the core principles and long-term goals of a resilient, scalable, and agile microservices architecture. The challenges related to data consistency and operational overhead will be addressed through careful design, event-driven patterns, and robust observability.