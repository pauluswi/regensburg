# ADR-005: Redis for Idempotency and Caching

## Status

Proposed

## Context

The Core Banking Middleware Modernization project, built on a microservices architecture, requires efficient mechanisms to handle two critical concerns:
1.  **Idempotency**: To prevent duplicate processing of requests (e.g., double-debiting an account) in a distributed system, especially when dealing with retries, network glitches, or asynchronous communication (as discussed in ADR-003 and ADR-004).
2.  **Caching**: To improve the performance and responsiveness of services by reducing the load on backend databases and other services, and by minimizing latency for frequently accessed data.

These requirements demand a fast, highly available, and scalable in-memory data store that can serve as a central repository for idempotency keys and cached data across multiple microservice instances.

## Decision

We will adopt **Redis** as the primary in-memory data store for implementing **idempotency checks** and **distributed caching** across the microservices in the Core Banking Middleware Modernization project.

## Consequences

### Positive

*   **High Performance**: Redis is an in-memory data store, offering extremely fast read and write operations (sub-millisecond latency), which is crucial for real-time idempotency checks and low-latency data retrieval from caches.
*   **Versatility**: Redis supports various data structures (strings, hashes, lists, sets, sorted sets), making it suitable for a wide range of caching strategies and for storing idempotency keys efficiently.
*   **Idempotency Implementation**: Its atomic operations (e.g., `SETNX` - Set if Not eXists) are ideal for implementing robust idempotency mechanisms, ensuring that a request is processed only once.
*   **Distributed Caching**: Provides a centralized cache accessible by all microservice instances, ensuring data consistency across the distributed system.
*   **Scalability and High Availability**: Redis supports replication (master-replica) for high availability and read scaling, and clustering for horizontal scaling of data across multiple nodes.
*   **Simplicity and Ease of Use**: Redis has a relatively simple API and is well-supported by client libraries in various programming languages, making it easy for developers to integrate.
*   **Cost-Effectiveness**: By offloading reads from primary databases, Redis can reduce the need for expensive database scaling, leading to cost savings.

### Negative

*   **Operational Overhead**: Managing and monitoring Redis instances, especially in a clustered, highly available setup, adds operational complexity. This includes configuration, backup/restore, and performance tuning.
*   **Data Volatility**: As an in-memory store, data in Redis is primarily volatile. While Redis offers persistence options (RDB snapshots, AOF logging), careful consideration is needed for data loss scenarios, especially for critical idempotency keys that might need to survive restarts.
*   **Memory Consumption**: Redis stores data in RAM, which can become a significant cost factor if large datasets are cached or if idempotency keys are retained for very long periods. Proper cache invalidation and key expiration strategies are essential.
*   **Single-Threaded Nature**: While highly performant, Redis is single-threaded. Long-running commands or inefficient data access patterns can block the server, impacting performance.
*   **Complexity for Advanced Use Cases**: While versatile, for extremely complex data grid requirements or advanced distributed computing patterns, other solutions might offer more specialized features (though often with higher complexity).

## Alternatives Considered

### 1. In-memory Caches (e.g., Caffeine, Guava Cache)

*   **Pros**: Extremely fast, no network overhead, no external dependency.
*   **Cons**: Not distributed; cache data is local to a single microservice instance. Data is lost on service restart. Not suitable for sharing idempotency keys or for distributed caching where consistency across instances is required.

### 2. Relational Database (e.g., PostgreSQL)

*   **Pros**: Data persistence, ACID properties, familiar to most developers.
*   **Cons**: Significantly slower for caching and idempotency checks due to disk I/O and network latency compared to in-memory stores. Adds load to the primary data store, potentially creating a bottleneck. Not designed for high-speed key-value lookups.

### 3. NoSQL Document Database (e.g., MongoDB, Cassandra)

*   **Pros**: Scalable, flexible schema.
*   **Cons**: Generally slower than Redis for simple key-value lookups and caching due to disk persistence and more complex data models. While faster than relational databases for some use cases, they typically don't match the raw speed of an in-memory store for caching and idempotency.

### 4. Other Distributed Caches (e.g., Hazelcast, Apache Ignite)

*   **Pros**: Feature-rich, often provide more advanced data grid capabilities, distributed computing features.
*   **Cons**: Can be more complex to set up, configure, and manage than Redis for the specific use cases of simple caching and idempotency. Redis often provides a simpler, more focused solution for these needs.

Redis was chosen for its unparalleled performance for key-value operations, its robust support for various data structures, and its proven ability to handle high-volume, low-latency requirements for both idempotency and distributed caching in modern microservices architectures. The operational challenges are considered manageable given the significant performance and resilience benefits.
