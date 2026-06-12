# ADR-007: Authentication and Authorization Mechanism

## Status

Proposed

## Context

In a microservices architecture, securing access to APIs and services is a critical concern. The Core Banking Middleware Modernization project exposes various APIs, both for internal microservice-to-microservice communication and for external clients (Mobile Banking, Internet Banking, partners). Without a robust and standardized authentication and authorization mechanism, the system is vulnerable to unauthorized access, data breaches, and compliance violations.

Key challenges include:
*   Ensuring secure communication between services.
*   Authenticating diverse client applications and users.
*   Authorizing access to specific resources based on roles or attributes.
*   Managing user identities and credentials.
*   Providing a scalable and performant security solution that doesn't become a bottleneck.

## Decision

We will implement a centralized, token-based authentication and authorization mechanism leveraging **OAuth 2.0** for delegated authorization and **OpenID Connect (OIDC)** for user authentication, with **JSON Web Tokens (JWTs)** as the primary token format. A dedicated **Identity Provider (IdP)** will manage user identities and issue tokens.

### 1. Core Standards

*   **OAuth 2.0**: Used for delegated authorization, allowing client applications to obtain limited access to user resources on behalf of the user.
*   **OpenID Connect (OIDC)**: An identity layer built on top of OAuth 2.0, providing user authentication and information about the end-user in an interoperable and REST-like manner.
*   **JSON Web Tokens (JWTs)**: Used as Bearer tokens for both access tokens (OAuth 2.0) and ID tokens (OIDC). JWTs are digitally signed, allowing microservices to validate them locally without needing to call the IdP for every request (unless revocation checks are required).

### 2. Identity Provider (IdP)

*   A dedicated, highly available Identity Provider (e.g., Keycloak, Auth0, AWS Cognito, or an in-house solution) will be used to:
    *   Manage user identities and credentials.
    *   Handle user authentication (login).
    *   Issue ID Tokens (for user identity) and Access Tokens (for authorization) following OIDC and OAuth 2.0 flows.
    *   Manage client applications and their credentials.
    *   Provide standard endpoints for token issuance, introspection, and revocation.

### 3. Authentication Flow

*   **External Clients (e.g., Mobile/Internet Banking)**:
    1.  Client initiates an OIDC Authorization Code Flow with PKCE (Proof Key for Code Exchange) with the IdP.
    2.  User authenticates with the IdP.
    3.  IdP issues an ID Token (user identity) and an Access Token (authorization) to the client.
    4.  Client sends the Access Token in the `Authorization: Bearer <token>` header to the API Gateway.
*   **Internal Service-to-Service Communication**:
    *   For synchronous calls, services will propagate the original Access Token (if applicable) or use mTLS for mutual authentication.
    *   For asynchronous (event-driven) communication, relevant security context (e.g., user ID, client ID) will be included in event headers if needed for downstream authorization.

### 4. Token Validation

*   **API Gateway**: The API Gateway will perform initial validation of incoming JWTs:
    *   Signature verification using the IdP's public key.
    *   Expiration check.
    *   Audience and issuer validation.
    *   Basic scope/permission checks.
*   **Microservices**: Individual microservices will also perform local validation of JWTs (signature, expiration, audience, issuer) to ensure the token's integrity and authenticity. They will then extract claims (e.g., user ID, roles, scopes) for authorization decisions.
*   **Token Revocation**: For critical operations or high-risk scenarios, services may implement token introspection or maintain a local cache of revoked tokens (e.g., using Redis) to handle immediate token invalidation.

### 5. Authorization Policies

*   **Role-Based Access Control (RBAC)**:
    *   Access to API endpoints and resources will primarily be controlled using RBAC.
    *   Roles (e.g., `teller`, `customer`, `admin`) will be defined and assigned to users within the IdP.
    *   Access Tokens will contain role claims, which microservices will use to determine if a user has permission to perform a specific action.
*   **Attribute-Based Access Control (ABAC)**:
    *   For more fine-grained authorization, ABAC may be applied where access decisions depend on specific attributes of the user, the resource, or the environment (e.g., "a user can only access their own account").
    *   These attributes can be included as claims in the JWT or retrieved from a policy decision point.
*   **Policy Enforcement Points (PEPs)**: Authorization logic will be implemented at various PEPs, including the API Gateway and within individual microservices.

## Consequences

### Positive

*   **Strong Security Posture**: Leverages industry-standard, well-vetted protocols (OAuth2, OIDC, JWT) for robust security.
*   **Decoupling**: Microservices are decoupled from user management and authentication logic, delegating it to the IdP.
*   **Scalability**: JWTs allow for stateless validation by microservices, reducing calls to the IdP and improving performance.
*   **Flexibility**: Supports various client types and authentication flows.
*   **Centralized Identity Management**: Simplifies user and client management through a single IdP.
*   **Auditability**: JWT claims provide context for auditing access attempts.

### Negative

*   **Increased Complexity**: Implementing and managing OAuth2/OIDC and an IdP adds initial complexity and a learning curve for developers.
*   **Operational Overhead**: The IdP itself needs to be highly available, secure, and managed.
*   **Token Revocation Challenges**: Stateless JWTs make immediate revocation difficult without additional mechanisms (e.g., short expiry times, introspection, blacklisting).
*   **Security of JWTs**: While signed, JWTs are not encrypted by default. Sensitive information should not be stored in JWT claims.
*   **Performance Overhead**: Signature verification and claim extraction add a small overhead to each request, though typically negligible compared to network latency.

## Alternatives Considered

### 1. API Key Authentication

*   **Pros**: Simple to implement for machine-to-machine communication.
*   **Cons**: Lacks user context, difficult to manage for a large number of users/clients, poor for delegated authorization, and less secure for user-facing applications. Only suitable for specific partner integrations.

### 2. Session-Based Authentication

*   **Pros**: Traditional, well-understood.
*   **Cons**: Not suitable for stateless microservices architectures, requires sticky sessions or a distributed session store, difficult to scale horizontally, and not ideal for mobile or partner integrations.

### 3. Basic Authentication

*   **Pros**: Very simple.
*   **Cons**: Sends credentials with every request, highly insecure without HTTPS, no concept of delegated authorization, not suitable for modern applications.

The chosen approach provides a balanced solution that meets the security, scalability, and flexibility requirements of a modern microservices-based core banking middleware, aligning with industry best practices.
