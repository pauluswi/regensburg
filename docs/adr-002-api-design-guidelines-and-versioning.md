# ADR-002: API Design Guidelines and Versioning

## Status

Proposed

## Context

The Core Banking Middleware Modernization project involves developing numerous microservices that expose APIs for internal and external consumption. To ensure consistency, maintainability, usability, and future extensibility across these services, it is crucial to establish clear guidelines for API design, documentation, and versioning. Without such standards, API consumers (both human developers and automated systems) will face increased complexity, integration challenges, and a higher risk of errors.

## Decision

We will adopt a set of comprehensive API Design Guidelines and a clear Versioning Strategy based on RESTful principles.

### 1. API Design Principles

*   **RESTful Design**: APIs will adhere to REST (Representational State Transfer) architectural style, utilizing standard HTTP methods (GET, POST, PUT, PATCH, DELETE) for resource manipulation.
*   **Resource-Oriented**: APIs will be designed around business resources (e.g., `/accounts`, `/transactions`, `/customers`) rather than actions.
*   **Statelessness**: Each request from client to server must contain all the information necessary to understand the request.
*   **Clear Naming Conventions**:
    *   **Resources**: Use plural nouns for collection resources (e.g., `/accounts`).
    *   **Resource Identifiers**: Use path segments for identifiers (e.g., `/accounts/{accountId}`).
    *   **Actions/Sub-resources**: Use nested resources or custom actions where appropriate (e.g., `/accounts/{accountId}/balance`, `/transactions:search`).
    *   **Fields**: Use `camelCase` for JSON/YAML field names.
*   **Idempotency**: Design APIs to be idempotent where applicable (e.g., PUT, DELETE) to allow safe retries.
*   **Pagination, Filtering, Sorting**: Provide standard mechanisms for clients to paginate, filter, and sort large collections of resources (e.g., query parameters like `?page=1&size=10`, `?status=active`, `?sort=createdAt:desc`).

### 2. Request and Response Formats

*   **JSON as Primary Format**: JSON (JavaScript Object Notation) will be the primary data interchange format for request and response bodies.
*   **Standard HTTP Status Codes**: Use appropriate HTTP status codes to indicate the outcome of an API request (e.g., `200 OK`, `201 Created`, `204 No Content`, `400 Bad Request`, `401 Unauthorized`, `403 Forbidden`, `404 Not Found`, `500 Internal Server Error`).
*   **Consistent Error Response Format**: All error responses will follow a standardized JSON structure, including fields like `code`, `message`, `details` (optional), and `timestamp`.

    ```json
    {
      "code": "BAD_REQUEST",
      "message": "Invalid input parameters",
      "details": [
        {
          "field": "amount",
          "error": "must be positive"
        }
      ],
      "timestamp": "2023-10-27T10:30:00Z"
    }
    ```

### 3. Authentication and Authorization Headers

*   **OAuth2/OIDC**: Authentication will primarily use OAuth2/OpenID Connect.
*   **Bearer Token**: Access tokens (JWTs) will be passed in the `Authorization` header using the `Bearer` scheme (e.g., `Authorization: Bearer <token>`).
*   **API Keys**: For specific external integrations, API keys might be used, passed via a custom HTTP header (e.g., `X-API-Key`).

### 4. API Versioning Strategy

*   **URI Versioning**: API versions will be included in the URI path (e.g., `/v1/accounts`, `/v2/transactions`). This is explicit, easily cacheable, and widely understood.
*   **Backward Compatibility**: Minor changes (e.g., adding new optional fields, new endpoints) should be backward compatible within a major version.
*   **Major Version Increment**: A new major version (`v2`, `v3`, etc.) will be introduced for any backward-incompatible changes (e.g., removing fields, changing data types, altering endpoint paths).
*   **Deprecation Policy**: When a new major version is released, the previous version will be deprecated and supported for a defined period (e.g., 6-12 months) to allow consumers to migrate.

### 5. API Documentation

*   **OpenAPI Specification (OAS/Swagger)**: All APIs will be documented using the OpenAPI Specification. This enables automated generation of client SDKs, server stubs, and interactive documentation.
*   **Centralized Documentation Portal**: A centralized portal will be used to host and discover API documentation.

## Consequences

### Positive

*   **Improved Developer Experience**: Consistent APIs are easier to understand, integrate with, and consume, reducing development time and errors for internal and external teams.
*   **Enhanced Maintainability**: Standardized design reduces cognitive load for developers working across different services and simplifies troubleshooting.
*   **Future Extensibility**: A clear versioning strategy allows for evolving APIs without breaking existing clients, facilitating continuous development and deployment.
*   **Automated Tooling**: OpenAPI documentation enables automation for testing, client generation, and API gateway configuration.
*   **Better Security Posture**: Consistent authentication header usage and error handling contribute to a more secure and predictable API landscape.

### Negative

*   **Initial Overhead**: Defining and enforcing guidelines requires an initial investment in time and effort for documentation, training, and code reviews.
*   **Strict Adherence**: Developers must strictly adhere to the guidelines, which might feel restrictive initially.
*   **Versioning Complexity**: Managing multiple API versions simultaneously can add operational overhead, especially during migration periods.

## Alternatives Considered

### 1. Header Versioning

*   **Description**: API version is specified in a custom HTTP header (e.g., `X-API-Version: 1`) or through the `Accept` header (e.g., `Accept: application/vnd.bankmas.v1+json`).
*   **Pros**: Cleaner URIs, allows clients to specify desired version without changing the URL.
*   **Cons**: Less discoverable, harder to test directly in browsers, can be more complex for load balancers/proxies to route based on headers.

### 2. Query Parameter Versioning

*   **Description**: API version is passed as a query parameter (e.g., `/accounts?version=1`).
*   **Pros**: Easy to implement.
*   **Cons**: Can be easily omitted, less RESTful as query parameters are typically for filtering/sorting, not resource identification.

### 3. No Formal Versioning

*   **Description**: APIs are evolved without explicit versioning, relying solely on backward compatibility.
*   **Pros**: Simplest approach initially.
*   **Cons**: Leads to significant breaking changes and integration nightmares as the system evolves, making it unsustainable for a large-scale project.

The URI versioning approach was chosen for its clarity, discoverability, and widespread adoption in the industry, balancing ease of use with explicit control over API evolution.
