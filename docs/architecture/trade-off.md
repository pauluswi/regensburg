# Architectural Decisions: Trade-offs Summary

This document summarizes the key architectural decisions, the chosen options, and the alternatives considered for the Core Banking Middleware Modernization project.

| ADR | Decision | Chosen | Alternatives Considered |
|:----|:---------|:-------|:------------------------|
| 001 | Java Version | Java 21 LTS | Java 17 LTS, Java 8 / Java 11 |
| 002 | API Design Guidelines and Versioning | Comprehensive API Design Guidelines and URI Versioning (RESTful) | Header Versioning, Query Parameter Versioning, No Formal Versioning |
| 003 | Microservice Communication Strategy | Hybrid (Synchronous REST/gRPC & Asynchronous Kafka) | Purely Synchronous, Purely Asynchronous, Enterprise Service Bus (ESB) |
| 004 | Event Streaming Platform | Apache Kafka | RabbitMQ, AWS Kinesis / Azure Event Hubs / Google Cloud Pub/Sub, Database-based Event Log |
| 005 | Idempotency and Caching Solution | Redis | In-memory Caches, Relational Database, NoSQL Document Database, Other Distributed Caches |
