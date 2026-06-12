# ADR-011: Data Migration Strategy from Legacy Systems

## Status

Proposed

## Context

The Core Banking Middleware Modernization project involves replacing or augmenting existing monolithic middleware and integrating with an unchanged Core Banking System. A critical aspect of this modernization is the safe, reliable, and non-disruptive migration of existing data from legacy systems to the new microservices' dedicated data stores (as per ADR-006). This is particularly challenging in a banking context where data integrity, consistency, and availability are paramount.

The "Progressive strangler migration" strategy (mentioned in `arc42_CBS_MIDDLEWARE_MODERNIZATION.md`) implies a gradual transition, which necessitates a robust data migration approach that supports coexistence and incremental cutovers without a "big bang" rewrite.

Without a clear data migration strategy, the project faces significant risks, including:
*   Data loss or corruption.
*   Extended downtime during cutover.
*   Inconsistent data states between old and new systems.
*   Increased complexity and cost of migration.
*   Failure to meet regulatory and business continuity requirements.

## Decision

We will adopt a phased, incremental data migration strategy that prioritizes data integrity, minimizes downtime, and supports the coexistence of legacy and modernized systems during the transition. The primary patterns will involve **Dual-Write (or Parallel Run)** and **Event-Driven Data Synchronization** for active data, complemented by **Batch Migration** for historical or static data.

### 1. Core Principles

*   **Data Integrity**: Ensure 100% data accuracy and consistency throughout the migration process.
*   **Minimal Downtime**: Strive for near-zero downtime for critical banking operations during cutover.
*   **Incremental Approach**: Migrate data in smaller, manageable chunks rather than a single large operation.
*   **Reversibility**: Design migration steps to be reversible where possible, allowing for rollback if issues arise.
*   **Observability**: Implement comprehensive monitoring and alerting for all migration processes.

### 2. Migration Patterns

#### 2.1. Dual-Write / Parallel Run (for Active Data)

*   **Purpose**: To ensure both legacy and new systems receive and process new data concurrently, allowing for validation and gradual traffic shifting.
*   **Mechanism**:
    1.  **Initial Load**: Perform an initial, one-time batch migration of existing data from the legacy system to the new microservices' databases. This will likely require a brief maintenance window.
    2.  **Change Data Capture (CDC) / Event Publication**: Implement mechanisms in the legacy system (or an intermediary layer) to capture all new data changes (inserts, updates, deletes) in real-time. These changes will be published as events to Kafka (as per ADR-004).
    3.  **Dual-Write Logic**: New microservices will consume these events and write the data to their own databases. Additionally, for new operations initiated through the modernized system, the microservice will write data to its own database *and* potentially to the legacy system (if the legacy system still needs to be the source of truth for a period).
    4.  **Validation**: Continuously compare data between legacy and new systems to identify discrepancies.
    5.  **Traffic Shifting**: Gradually shift read and write traffic from the legacy system to the new microservices using the Strangler Fig Pattern.
*   **Applicability**: Critical, frequently changing data (e.g., customer accounts, transactions, balances).

#### 2.2. Event-Driven Data Synchronization (for Active Data)

*   **Purpose**: To keep data eventually consistent across services and between legacy and new systems, especially when the legacy system remains the source of truth for certain domains.
*   **Mechanism**: The legacy system (or an adapter) publishes domain events to Kafka whenever relevant data changes. New microservices subscribe to these events and update their local materialized views or data copies.
*   **Applicability**: Data that needs to be replicated or denormalized across multiple microservices, or data where the legacy system remains authoritative for an extended period.

#### 2.3. Batch Migration (for Historical/Static Data)

*   **Purpose**: For large volumes of historical data or relatively static reference data that does not change frequently.
*   **Mechanism**: Extract, Transform, Load (ETL) processes will be used to move data from legacy data stores to new microservices' databases. This can be done offline or during scheduled maintenance windows.
*   **Applicability**: Historical transaction records, archived customer data, product catalogs, reference data.

### 3. Tooling and Implementation

*   **Kafka**: Central event bus for CDC and event-driven synchronization.
*   **Custom Adapters/Connectors**: Develop specific adapters to extract data changes from legacy systems and publish them to Kafka.
*   **ETL Tools**: Utilize existing or new ETL tools for batch migration.
*   **Database Migration Tools**: Use tools like Flyway or Liquibase for managing schema changes in the new microservices' databases.
*   **Monitoring**: Integrate migration progress and data consistency checks into the observability stack (ADR-009).

## Consequences

### Positive

*   **Reduced Risk**: Incremental approach minimizes the risk of catastrophic failure during migration.
*   **Minimal Downtime**: Dual-write and event-driven patterns allow for continuous operation during transition.
*   **Data Consistency**: Continuous validation and synchronization mechanisms help maintain data integrity.
*   **Supports Strangler Fig**: Directly enables the progressive strangler migration pattern by allowing old and new systems to coexist.
*   **Flexibility**: Allows different migration strategies for different data types based on their criticality and change frequency.

### Negative

*   **Increased Complexity**: Implementing dual-write and CDC mechanisms can be complex, especially with legacy systems.
*   **Temporary Data Duplication**: Data will exist in both legacy and new systems for a period, requiring careful management.
*   **Performance Overhead**: CDC and dual-write operations can add overhead to both legacy and new systems.
*   **Rollback Complexity**: While designed for reversibility, rolling back a partial migration can still be challenging.
*   **Validation Effort**: Significant effort is required for continuous data validation and reconciliation.
*   **Potential for Data Drift**: If not carefully managed, there's a risk of data inconsistencies between systems.

## Alternatives Considered

### 1. Big Bang Migration

*   **Description**: Stop all operations, migrate all data at once, and then switch to the new system.
*   **Pros**: Simpler in terms of managing two systems concurrently.
*   **Cons**: Extremely high risk, requires significant downtime, not feasible for critical 24/7 banking systems. Rejected due to unacceptable business risk.

### 2. Manual Data Entry/Reconciliation

*   **Description**: Manually re-entering data into the new system or relying heavily on manual reconciliation.
*   **Pros**: Low technical complexity.
*   **Cons**: Extremely error-prone, time-consuming, not scalable, and unacceptable for large volumes of critical banking data. Rejected.

The chosen phased migration strategy, leveraging dual-write, event-driven synchronization, and batch migration, provides the most balanced approach for safely and effectively transitioning data in a complex core banking modernization project, aligning with the project's goals for minimal disruption and high data integrity.
