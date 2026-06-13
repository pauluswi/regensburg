# Architectural Decisions: Trade-offs Summary

This document summarizes the key architectural decisions, the chosen options, and the alternatives considered for the Core Banking Middleware Modernization project.

| ADR | Decision | Chosen | Alternatives Considered |
|:----|:---------|:-------|:------------------------|
| 001 | Java Version | Java 21 LTS | Java 17 LTS, Java 8 / Java 11 |
| 002 | API Design Guidelines and Versioning | Comprehensive API Design Guidelines and URI Versioning (RESTful) | Header Versioning, Query Parameter Versioning, No Formal Versioning |
| 003 | Microservice Communication Strategy | Hybrid (Synchronous REST/gRPC & Asynchronous Kafka) | Purely Synchronous, Purely Asynchronous, Enterprise Service Bus (ESB) |
| 004 | Event Streaming Platform | Apache Kafka | RabbitMQ, AWS Kinesis / Azure Event Hubs / Google Cloud Pub/Sub, Database-based Event Log |
| 005 | Idempotency and Caching Solution | Redis | In-memory Caches, Relational Database, NoSQL Document Database, Other Distributed Caches |
| 006 | Database Strategy for Microservices | "Database per Service" strategy | Shared Database, Monolithic Database |
| 007 | Authentication and Authorization Mechanism | OAuth 2.0, OpenID Connect, JWTs with dedicated IdP | API Key Authentication, Session-Based Authentication, Basic Authentication |
| 008 | Error Handling and Resilience Patterns | Standardized Resilience Patterns (Circuit Breaker, Retry, Timeout, Bulkhead, Rate Limiter) | Ad-hoc Error Handling, Centralized Resilience Library/Framework (without formal ADR) |
| 009 | Centralized Observability Stack | Integrated ELK/Loki, Prometheus/Grafana, OpenTelemetry/Jaeger/Tempo | No Centralized Observability, Commercial Observability Platforms |
| 010 | Containerization and Deployment Strategy | Docker for Containerization, Kubernetes for Orchestration | Virtual Machines (VMs) with Traditional Deployment, Serverless Platforms, Docker Swarm |
| 011 | Data Migration Strategy from Legacy Systems | Phased, Incremental (Dual-Write, Event-Driven Sync, Batch) | Big Bang Migration, Manual Data Entry/Reconciliation |
| 012 | API Gateway Product Selection and Responsibilities | Spring Cloud Gateway (with conditional Istio Gateway) | No API Gateway (Direct Service Exposure), Commercial API Gateway Products, Nginx/HAProxy as a Reverse Proxy |
| 013 | Configuration Management Strategy | Layered (Vault for Secrets, K8s ConfigMaps, Spring Cloud Config) | Environment Variables / Command Line Arguments, Hardcoding Configurations, Cloud Provider Specific |
| 014 | Testing Strategy for Microservices | Multi-faceted testing strategy (Unit, Integration, Contract, E2E, Performance, Chaos, Security) | Heavy End-to-End Testing, Minimal Testing, Manual-Only Testing |
| 015 | Migration Strategy (Deep Dive into Strangler Fig) | Strangler Fig Pattern (Incremental Replacement, Coexistence, Traffic Interception) | Big Bang Rewrite, Rebuild from Scratch (Greenfield) with Coexistence |
| 016 | Integration Adapter Design (for ISO 8583/20022) | Dedicated Integration Adapters as Microservices | Direct Integration within Business Microservices, Commercial Integration Platform (ESB-like) |
| 017 | CI/CD Pipeline Architecture | Robust, Automated CI/CD Pipeline (Declarative, Version-Controlled, GitOps) | Manual Deployment, Basic Scripted CI/CD |
