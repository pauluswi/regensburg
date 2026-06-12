# ADR-015: Migration Strategy (Deep Dive into Strangler Fig)

## Status

Proposed

## Context

The Core Banking Middleware Modernization project aims to transform a monolithic middleware into a cloud-native microservices architecture. This is a complex undertaking, especially in a highly regulated and critical environment like banking, where system availability and data integrity are paramount. A "big bang" rewrite (replacing the entire system at once) carries unacceptable risks, including prolonged downtime, massive cost overruns, and high failure rates.

The `arc42_CBS_MIDDLEWARE_MODERNIZATION.md` document explicitly lists "Progressive strangler migration" as a key solution strategy. This ADR provides a deeper dive into this strategy, detailing how legacy functionalities will be gradually replaced without disrupting ongoing operations, thereby minimizing risk and ensuring continuous business value delivery.

## Decision

We will adopt the **Strangler Fig Pattern** as the primary migration strategy for transitioning from the monolithic banking middleware to a microservices architecture. This pattern involves incrementally replacing functionalities of the legacy system with new microservices, routing traffic to the new components, and eventually "strangling" the old system until it can be safely decommissioned.

### 1. Core Principles of Strangler Fig

*   **Incremental Replacement**: Functionalities are migrated piece by piece, not all at once.
*   **Coexistence**: The legacy system and new microservices operate simultaneously during the transition.
*   **Traffic Interception**: An intermediary (typically an API Gateway or reverse proxy) is used to redirect traffic from the legacy system to the new microservices.
*   **Minimizing Risk**: Each migration step is small, testable, and reversible, reducing the impact of potential failures.
*   **Continuous Delivery**: Enables continuous development and deployment of new functionalities while the migration is ongoing.

### 2. Phases of Migration

The migration will typically follow these iterative phases for each identified domain or functionality:

1.  **Identify a Bounded Context/Domain**: Select a specific business capability within the monolith that can be extracted into a new microservice. Prioritize areas with high business value, frequent changes, or significant pain points.
2.  **Build the New Microservice**: Develop the new microservice with its own dedicated data store (as per ADR-006) and APIs (as per ADR-002), implementing the chosen functionality.
3.  **Implement Data Synchronization**:
    *   **Initial Data Load**: Migrate existing relevant data from the legacy system to the new microservice's database (as per ADR-011).
    *   **Continuous Data Synchronization**: Implement mechanisms (e.g., Change Data Capture (CDC) from legacy DB, event publication from legacy system) to keep the new microservice's data consistent with the legacy system for active data (as per ADR-011).
    *   **Dual-Write**: For new writes, implement logic to write to both the new microservice's database and the legacy system's database during a transition period (as per ADR-011).
4.  **Redirect Traffic**:
    *   Configure the API Gateway (as per ADR-012) or a reverse proxy to intercept requests for the migrated functionality.
    *   Gradually redirect a portion of the traffic (e.g., 1%, then 5%, then 10%, etc.) from the legacy system to the new microservice.
    *   Monitor the new microservice closely (as per ADR-009) for performance, errors, and functional correctness.
5.  **Validate and Iterate**:
    *   Thoroughly test the new microservice and its integration with the rest of the system (as per ADR-014).
    *   Gather feedback, address issues, and refine the microservice.
    *   Once confident, increase the redirected traffic until 100% of traffic goes to the new microservice.
6.  **Decommission Legacy Functionality**: Once the new microservice is stable, handles all relevant traffic, and the legacy functionality is no longer needed, remove the corresponding code from the monolith and eventually decommission the legacy component.

### 3. Key Techniques and Enablers

*   **API Gateway (ADR-012)**: Crucial for traffic routing, interception, and managing the transition between old and new services.
*   **Event-Driven Architecture (ADR-004)**: Facilitates loose coupling and real-time data synchronization between legacy and new systems.
*   **Data Migration Strategy (ADR-011)**: Defines how data is moved and kept consistent during the transition.
*   **Observability Stack (ADR-009)**: Essential for monitoring the health and performance of both old and new systems during migration and detecting issues quickly.
*   **Resilience Patterns (ADR-008)**: Protects the system during the transition, especially when dealing with interactions between old and new components.
*   **Automated CI/CD (ADR-010)**: Enables rapid and reliable deployment of new microservices and configuration changes.

## Consequences

### Positive

*   **Reduced Risk**: Small, incremental changes minimize the risk of large-scale failures and allow for quick rollbacks.
*   **Continuous Business Value**: New features can be delivered and deployed independently, even during the migration.
*   **Minimal Downtime**: Traffic redirection and coexistence ensure continuous operation.
*   **Learning and Adaptation**: Teams gain experience with microservices and new technologies gradually.
*   **Improved System Quality**: New microservices are built with modern practices, leading to better scalability, resilience, and maintainability.
*   **Faster Time to Market**: New capabilities can be delivered more quickly once the new architecture is in place.

### Negative

*   **Increased Complexity During Transition**: Operating and maintaining both the legacy monolith and the new microservices simultaneously adds complexity.
*   **Data Synchronization Challenges**: Ensuring data consistency between old and new systems can be difficult and requires careful design (ADR-011).
*   **Operational Overhead**: Requires robust monitoring and traffic management during the migration period.
*   **Temporary Duplication**: Some functionalities or data might be duplicated for a period.
*   **Requires Discipline**: Strict adherence to the pattern and clear boundaries are necessary to avoid creating a "distributed monolith."

## Alternatives Considered

### 1. Big Bang Rewrite

*   **Description**: Stop the old system, rewrite the entire application from scratch, and then deploy the new system.
*   **Pros**: Clean break from legacy code.
*   **Cons**: Extremely high risk, very long development cycles, high probability of failure, unacceptable downtime for a core banking system. Rejected.

### 2. Rebuild from Scratch (Greenfield) with Coexistence

*   **Description**: Build a completely new system alongside the old one, and then switch over once the new system is complete.
*   **Pros**: No need to touch legacy code, can leverage all new technologies.
*   **Cons**: High cost of maintaining two systems for a long period, still involves a large cutover event (though potentially less risky than a big bang rewrite), and delays value delivery. The Strangler Fig is preferred as it delivers value incrementally.

The Strangler Fig Pattern is chosen as it provides the most pragmatic and least risky approach for modernizing the core banking middleware, allowing for continuous delivery of value while systematically dismantling the legacy monolith.
