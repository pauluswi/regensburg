# arc42 – Core Banking Middleware Modernization

Authors: IT Software Architecture Team

![Status](https://img.shields.io/badge/Status-Final-green)
![Version](https://img.shields.io/badge/Version-1.0-blue)
![Java](https://img.shields.io/badge/Java-21_LTS-orange)
![Deployment](https://img.shields.io/badge/Deployment-Kubernetes-326CE5?logo=kubernetes&logoColor=white)
![Eventing](https://img.shields.io/badge/Eventing-Kafka-231F20?logo=apachekafka&logoColor=white)
![Cache/Idempotency](https://img.shields.io/badge/Cache/Idempotency-Redis-DC382D?logo=redis&logoColor=white)
![Framework](https://img.shields.io/badge/Framework-Spring_Boot-6DB33F?logo=springboot&logoColor=white)

---

# 1. Introduction and Goals

## 1.1 Requirements Overview

The bank intends to modernize its middleware layer to support digital banking initiatives while reducing dependency on tightly coupled legacy integrations.

The middleware shall provide:

* Real-time transaction processing.
* Standardized integration with Core Banking.
* Open API enablement.
* ISO 8583 and ISO 20022 interoperability.
* Event-driven architecture capabilities.
* High availability and disaster recovery.
* Security aligned with banking regulations.
* Scalability for increasing digital transactions.

---

## 1.2 Quality Goals

| Priority | Quality Attribute | Description                              |
| -------- | ----------------- | ---------------------------------------- |
| 1        | Availability      | 99.99% uptime                            |
| 2        | Reliability       | No message loss                          |
| 3        | Performance       | Sub-second response for synchronous APIs |
| 4        | Scalability       | Horizontal scaling                       |
| 5        | Security          | Zero Trust principles                    |
| 6        | Maintainability   | Independent service deployment           |
| 7        | Observability     | End-to-end tracing                       |

---

## 1.3 Stakeholders

| Stakeholder       | Concerns                      |
| ----------------- | ----------------------------- |
| Business          | Faster product delivery       |
| Operations        | Stable production             |
| Compliance        | Auditability                  |
| Development Teams | Reusable integration services |
| Security Team     | Secure transactions           |
| Customers         | Reliable digital experience   |

---

# 2. Architecture Constraints

Technical constraints:

* Existing Core Banking System remains unchanged.
* Existing ISO 8583 connections must be preserved.
* Compliance with Bank Indonesia regulations.
* Support hybrid infrastructure.
* Existing channels cannot experience prolonged downtime.

Organizational constraints:

* Gradual migration approach.
* Limited specialist resources.
* Multiple vendor integrations.

---

# 3. System Scope and Context

## Business Context

External actors:

* Mobile Banking
* Internet Banking
* Branch Applications
* ATM Switch
* QRIS Providers
* BI-FAST
* Payment Gateway Partners
* Fraud Detection Systems
* Core Banking System

The middleware acts as the integration backbone.

---

## Technical Context

Middleware communicates using:

* REST APIs
* Kafka Events
* ISO 8583 TCP/IP
* ISO 20022 XML
* SFTP
* Database adapters

---

# 4. Solution Strategy

The modernization strategy consists of:

1. API-first integration.
2. Domain-oriented services.
3. Event-driven architecture.
4. Legacy encapsulation.
5. Cloud-native deployment.
6. Progressive strangler migration.
7. Centralized observability.
8. Security by design.

---

# 5. Building Block View

## Level 1 – System Context (C4 Context Diagram)

```
                 +-------------------+    +-------------------+
                 | Mobile Banking    |    | Internet Banking  |
                 +---------+---------+    +---------+---------+
                           |                        |
                           +-----------v------------+
                                       |
                               +---------v---------+
                               | API Gateway       |
                               +---------+---------+
                                         |
                               +-----------v------------+
                               | Banking Middleware     |
                               | Modernization Platform |
                               +-----------+------------+
                                         |
                   +-----------------+-----------------+
                   |                 |                 |
                   v                 v                 v
            Core Banking      Payment Networks   Event Platform
                               (ISO8583/20022)      (Kafka)
```

---

## Level 2 – Container Diagram

```
+--------------------------------------------------------+
| Middleware Platform                                    |
|                                                        |
|  +----------------+                                   |
|  | API Gateway    |                                   |
|  +--------+-------+                                   |
|           |                                           |
|  +--------v-------+                                   |
|  | Channel APIs   |                                   |
|  +--------+-------+                                   |
|           |                                           |
|  +--------v-------+                                   |
|  | Orchestration  |                                   |
|  +--------+-------+                                   |
|           |                                           |
| +---------+----------+---------+---------+            |
| |                    |         |         |            |
| v                    v         v         v            |
| ISO8583 Adapter  ISO20022  Core CBS  Notification     |
|                  Adapter   Adapter   Service          |
|                                                        |
| +-----------------------------------------------+      |
| | Kafka Event Streaming Platform                |      |
| +-----------------------------------------------+      |
|                                                        |
| +-----------------------------------------------+      |
| | Observability Stack                           |      |
| | Prometheus, Grafana, ELK, Tracing             |      |
| +-----------------------------------------------+      |
+--------------------------------------------------------+
```

---

## Level 3 – Component Diagram (Orchestration Service)

```
Orchestration Service

├── Transaction Router
├── Saga Coordinator
├── Validation Engine
├── Idempotency Manager
├── Retry Handler
├── Compensation Handler
├── Event Publisher
└── Audit Logger
```

---

# 6. Runtime View

## Funds Transfer Scenario

```
Mobile Banking

    |

API Gateway

    |

Transaction API

    |

Validation Engine

    |

Orchestrator

    |

Core Banking Adapter

    |

Core Banking

    |

Kafka Event Publication

    |

Notification Service

    |

Customer Notification
```

---

## QRIS Payment Scenario

```
QRIS Channel

    |

API Gateway

    |

QRIS Service

    |

ISO20022 Adapter

    |

BI-FAST / QRIS Switch

    |

Response returned
```

---

# 7. Deployment View

## Production Environment

```
Region A (Primary)

Kubernetes Cluster

├── API Gateway Pods
├── Middleware Pods
├── Kafka Cluster
├── Redis Cluster
├── Monitoring Stack

Database Cluster

Region B (DR)

Warm Standby Environment
```

Deployment principles:

* Multi-AZ deployment.
* Rolling updates.
* Blue-Green releases.
* Automated failover.

---

# 8. Cross-Cutting Concepts

## Security

* OAuth2/OIDC.
* JWT validation.
* Mutual TLS.
* Secrets management.
* WAF integration.
* Audit logging.

---

## Reliability

* Circuit Breaker.
* Retry Pattern.
* Dead Letter Queue.
* Timeout policies.
* Bulkhead isolation.

---

## Data Consistency

* Saga Pattern.
* Outbox Pattern.
* Idempotency Keys.

---

## Observability

* Distributed tracing.
* Correlation IDs.
* Metrics collection.
* Centralized logging.

---

# 9. Architecture Decisions

ADR references:

* ADR-001: Java Version 17 vs 21
* ADR-002: API Design Guidelines and Versioning
* ADR-003: Microservice Communication Strategy (Synchronous vs. Asynchronous)
* ADR-004: Event Streaming using Kafka
* ADR-005: Redis for Idempotency and Caching
* ADR-006: Database Strategy for Microservices
* ADR-007: Authentication and Authorization Mechanism
* ADR-008: Error Handling and Resilience Patterns
* ADR-009: Centralized Logging, Monitoring, and Tracing (Observability Stack)
* ADR-010: Containerization and Deployment Strategy
* ADR-011: Data Migration Strategy from Legacy Systems
* ADR-012: API Gateway Product Selection and Responsibilities
* ADR-013: Configuration Management Strategy
* ADR-014: Testing Strategy for Microservices

---

# 10. Quality Requirements

## Availability

Target: 99.99%

---

## Performance

API Response:

P95 < 500 ms

ISO8583 Response:

P95 < 2 seconds

---

## Scalability

Support:

* 2,000 TPS initial capacity.
* 10,000 TPS target capacity.

---

## Recovery Objectives

RTO: 30 minutes

RPO: < 5 minutes

---

# 11. Risks and Technical Debt

| Risk                   | Mitigation          |
| ---------------------- | ------------------- |
| Legacy CBS limitations | Adapter abstraction |
| Skill gaps             | Training program    |
| Integration complexity | Domain ownership    |
| Event duplication      | Idempotency design  |
| DR synchronization     | Regular DR testing  |

---

# 12. Glossary

| Term      | Meaning                              |
| --------- | ------------------------------------ |
| CBS       | Core Banking System                  |
| TPS       | Transactions Per Second              |
| QRIS      | Quick Response Indonesian Standard   |
| BI-FAST   | Indonesian real-time payment rail    |
| ADR       | Architecture Decision Record         |
| DLQ       | Dead Letter Queue                    |
| Saga      | Distributed transaction coordination |
| ISO 8583  | Card transaction messaging standard  |
| ISO 20022 | Financial messaging standard         |

---
