# ADR-016: Integration Adapter Design (for ISO 8583/20022)

## Status

Proposed

## Context

The Core Banking Middleware Modernization project requires robust and compliant integration with various external financial institutions and the existing Core Banking System. This often involves specialized financial messaging protocols such as ISO 8583 (for card-based transactions) and ISO 20022 (a newer, XML-based financial messaging standard). These protocols are complex, highly structured, and often require specific transport mechanisms (e.g., raw TCP/IP for ISO 8583).

Building reliable integration services for these protocols is critical for the bank's operations. Without a standardized approach to designing these adapters, there is a risk of:
*   Inconsistent message handling and data transformation.
*   Security vulnerabilities due to improper protocol implementation.
*   Difficulty in maintaining and extending integration points.
*   Compliance issues with financial regulations.
*   Increased operational burden for troubleshooting and error resolution.

This ADR defines the design principles, common components, and specific considerations for building robust and compliant integration adapters for ISO 8583 and ISO 20022 within the microservices architecture.

## Decision

We will design and implement dedicated **Integration Adapters** as microservices for each specific financial messaging protocol (e.g., ISO 8583, ISO 20022). These adapters will encapsulate the complexities of the external protocols, translating them into the internal domain model and API contracts of our microservices ecosystem.

### 1. Core Design Principles

*   **Encapsulation**: Each adapter will fully encapsulate the external protocol's complexities, exposing a clean, internal API (REST or event-based) to other microservices.
*   **Single Responsibility**: Each adapter will focus on a specific external protocol or external system integration.
*   **Statelessness (Internal)**: Adapters should strive to be stateless internally where possible, relying on external stores (e.g., Redis for session management) for state.
*   **Resilience**: Adapters must be highly resilient to network issues, external system failures, and malformed messages (as per ADR-008).
*   **Observability**: Comprehensive logging, monitoring, and tracing will be implemented to track message flow and identify issues (as per ADR-009).
*   **Security**: Adhere to strict security standards for data in transit and at rest.

### 2. Common Components and Considerations

#### 2.1. Message Parsing and Serialization

*   **ISO 8583**:
    *   Utilize a robust ISO 8583 library (e.g., J8583, jPOS) for parsing incoming raw byte streams and serializing outgoing messages.
    *   Support different ISO 8583 versions and custom field definitions (bitmaps).
    *   Handle ASCII/EBCDIC encoding variations.
*   **ISO 20022**:
    *   Utilize XML parsing libraries (e.g., JAXB, Jackson XML) for handling incoming XML messages.
    *   Validate messages against XSD schemas.
    *   Support different ISO 20022 message definitions (e.g., pain.001, pacs.008).

#### 2.2. Data Transformation

*   **Mapping Layer**: A clear mapping layer will translate between the external protocol's message structure and the internal domain model (e.g., Java POJOs, DTOs).
*   **Validation**: Perform both schema-level validation (e.g., XML Schema) and business-level validation on transformed data.
*   **Enrichment**: Potentially enrich incoming data with internal context before forwarding.

#### 2.3. Protocol and Transport Handling

*   **ISO 8583**:
    *   **Transport**: Typically raw TCP/IP sockets. Adapters will manage persistent connections, connection pooling, and reconnection logic.
    *   **Message Length Prefix**: Handle 2-byte or 4-byte message length prefixes.
    *   **Keep-alives**: Implement mechanisms for sending/receiving keep-alive messages.
    *   **Session Management**: For stateful connections, manage session state (e.g., using Redis for distributed session management).
*   **ISO 20022**:
    *   **Transport**: Often over HTTPS, SFTP, or message queues. Adapters will integrate with the chosen transport mechanism.
    *   **Acknowledgement**: Handle synchronous (HTTP 200 OK) or asynchronous (ACK/NACK messages) acknowledgements.

#### 2.4. Error Handling and Retries

*   **Protocol-Specific Errors**: Translate external protocol error codes into internal, standardized error responses.
*   **Idempotency**: Ensure that messages processed by adapters are idempotent, especially for retries (as per ADR-005).
*   **Retry Mechanisms**: Implement retry logic with exponential backoff for transient errors when communicating with external systems (as per ADR-008).
*   **Dead Letter Queues (DLQ)**: Utilize DLQs for messages that cannot be processed or delivered after multiple retries (as per ADR-008).

#### 2.5. Security

*   **Encryption**: Ensure all communication with external systems is encrypted (e.g., TLS for TCP/IP, HTTPS).
*   **Authentication**: Implement mutual TLS (mTLS) or other agreed-upon authentication mechanisms with external partners.
*   **Data Masking/Tokenization**: Mask or tokenize sensitive data fields before internal processing or logging.
*   **Input Validation**: Strict validation of all incoming message fields to prevent injection attacks or malformed data.

#### 2.6. Observability

*   **Logging**: Log incoming/outgoing messages (with sensitive data masked), transformation results, and errors (as per ADR-009).
*   **Metrics**: Collect metrics on message rates, processing times, error rates, and connection status.
*   **Tracing**: Propagate correlation IDs through the adapter for end-to-end tracing (as per ADR-009).

### 3. Integration with Microservices Ecosystem

*   **Internal API**: Adapters will expose internal APIs (e.g., REST endpoints for synchronous requests, Kafka topics for event publication/consumption) to other microservices.
*   **Event Publication**: Adapters will publish events to Kafka (as per ADR-004) for significant external events (e.g., "ISO8583_TransactionReceived", "ISO20022_PaymentInitiated").

## Consequences

### Positive

*   **Protocol Abstraction**: Microservices are shielded from the complexities of external financial protocols, simplifying their development.
*   **Consistency**: Standardized approach ensures consistent handling of external integrations.
*   **Enhanced Security**: Centralized security measures for external communication.
*   **Improved Maintainability**: Changes to external protocols are isolated to the adapter, reducing impact on core business logic.
*   **Increased Reliability**: Robust error handling and resilience patterns ensure stable integration.
*   **Faster Development**: Reusable components and clear patterns accelerate the development of new integrations.
*   **Compliance**: Easier to ensure adherence to financial messaging standards and regulations.

### Negative

*   **Increased Initial Development**: Building robust adapters requires specialized knowledge and can be time-consuming.
*   **Operational Overhead**: Each adapter is a separate microservice that needs to be deployed, monitored, and managed.
*   **Performance Overhead**: Data transformation and validation introduce some processing overhead.
*   **Complexity of Libraries**: Working with ISO 8583/20022 libraries can have a learning curve.

## Alternatives Considered

### 1. Direct Integration within Business Microservices

*   **Description**: Each business microservice directly implements the logic to communicate with external financial protocols.
*   **Pros**: No separate adapter service.
*   **Cons**: Duplication of protocol handling logic across multiple services, tight coupling to external protocols, increased complexity for business microservices, harder to maintain, and higher risk of inconsistent security/error handling. Rejected.

### 2. Commercial Integration Platform (ESB-like)

*   **Description**: Use a commercial Enterprise Service Bus (ESB) or integration platform to handle all external protocol integrations.
*   **Pros**: Often provides rich out-of-the-box connectors and transformation capabilities.
*   **Cons**: Can lead to vendor lock-in, potential for becoming a centralized bottleneck, often heavy and complex, contradicts the decentralized nature of microservices. Rejected in favor of lightweight, microservice-native adapters.

The dedicated Integration Adapter design is chosen as it provides the optimal balance between encapsulating complexity, ensuring compliance and security, and maintaining the agility and scalability benefits of the microservices architecture.
