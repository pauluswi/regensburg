# ADR-013: Configuration Management Strategy

## Status

Proposed

## Context

In a microservices architecture, applications require various configurations, including database connection strings, external service URLs, API keys, feature flags, and environment-specific settings. These configurations often differ across development, staging, and production environments. Managing these configurations consistently, securely, and efficiently is crucial for the Core Banking Middleware Modernization project to ensure:
*   Reproducibility of deployments across environments.
*   Security of sensitive information (secrets).
*   Agility in updating configurations without redeploying services.
*   Reduced operational overhead and human error.

Without a standardized configuration management strategy, microservices risk hardcoding values, using insecure practices for secrets, or facing inconsistencies that lead to runtime errors and deployment failures.

## Decision

We will implement a layered configuration management strategy leveraging **HashiCorp Vault** for secrets, **Kubernetes ConfigMaps** for non-sensitive, environment-specific configurations, and **Spring Cloud Config** (or similar framework-specific mechanisms) for application-level configuration externalization.

### 1. Secrets Management: HashiCorp Vault

*   **Purpose**: Securely store, access, and manage sensitive data (e.g., database credentials, API keys, private certificates).
*   **Mechanism**:
    *   Vault will be deployed as a dedicated, highly available service.
    *   Microservices will authenticate with Vault (e.g., using Kubernetes service accounts, JWTs) to dynamically retrieve secrets at runtime.
    *   Vault's dynamic secrets capabilities will be utilized where possible (e.g., generating short-lived database credentials).
    *   Secrets will be encrypted at rest and in transit.
    *   Access to secrets will be controlled via Vault's fine-grained access control policies.
*   **Integration**: Services will integrate with Vault clients (e.g., Spring Cloud Vault Config for Spring Boot applications) to fetch secrets. Kubernetes sidecar injection (e.g., Vault Agent Injector) will be used to simplify secret delivery to pods.

### 2. Non-Sensitive Configuration: Kubernetes ConfigMaps

*   **Purpose**: Store non-sensitive configuration data (e.g., application properties, environment variables, log levels, feature flags) that are specific to a Kubernetes environment.
*   **Mechanism**:
    *   ConfigMaps will be defined in YAML files and managed in a Git repository (GitOps approach).
    *   They will be mounted as files or injected as environment variables into microservice pods.
    *   ConfigMaps will be namespaced to separate configurations per environment or application domain.
*   **Integration**: Microservices will read configurations from mounted files or environment variables provided by ConfigMaps.

### 3. Application-Level Configuration: Spring Cloud Config (or similar)

*   **Purpose**: Provide a centralized externalization point for application-specific configurations, especially for Spring Boot applications, allowing for dynamic updates without redeployment.
*   **Mechanism**:
    *   A Spring Cloud Config Server will be deployed, backed by a Git repository containing application configuration files (e.g., `application.yml`, `service-name.yml`).
    *   Microservices will act as Config Clients, connecting to the Config Server to fetch their configurations at startup and potentially refresh them at runtime.
    *   Profiles will be used to manage environment-specific configurations (e.g., `application-dev.yml`, `application-prod.yml`).
*   **Integration**: Spring Boot applications will use `spring-cloud-starter-config` to integrate with the Config Server.

### 4. CI/CD and GitOps Integration

*   All configuration definitions (for ConfigMaps, Vault policies, Spring Cloud Config Git repository) will be version-controlled in Git.
*   Changes to configurations will follow a GitOps workflow, where pull requests are reviewed, merged, and then automatically applied to the respective environments.

## Consequences

### Positive

*   **Enhanced Security**: HashiCorp Vault provides a robust, industry-standard solution for managing secrets, significantly reducing the risk of sensitive data exposure.
*   **Consistency Across Environments**: Standardized approach ensures that configurations are managed uniformly, reducing "works on my machine" issues.
*   **Agility and Dynamic Updates**: Allows for updating non-sensitive configurations and secrets without requiring a full microservice redeployment, enabling faster responses to changes.
*   **Reduced Human Error**: Automation through GitOps and dedicated tools minimizes manual configuration errors.
*   **Improved Auditability**: All configuration changes are version-controlled in Git and Vault provides an audit log for secret access.
*   **Decoupling**: Separates configuration from application code, promoting better modularity.

### Negative

*   **Increased Complexity**: Implementing and managing Vault, Kubernetes ConfigMaps, and Spring Cloud Config adds significant architectural and operational complexity.
*   **Operational Overhead**: Vault and Spring Cloud Config Server instances need to be deployed, managed, secured, and kept highly available.
*   **Learning Curve**: Developers and operations teams need to learn new tools and concepts.
*   **Potential for Configuration Drift**: If not strictly enforced, manual overrides or inconsistent practices can lead to configuration drift.
*   **Startup Latency**: Services might experience slight startup latency while fetching configurations and secrets from external sources.

## Alternatives Considered

### 1. Environment Variables / Command Line Arguments

*   **Pros**: Simple to implement, native to operating systems and containers.
*   **Cons**: Not suitable for large numbers of configurations, difficult to manage across many services/environments, insecure for secrets (visible in process lists), limited dynamic update capabilities.

### 2. Hardcoding Configurations

*   **Pros**: Easiest initially.
*   **Cons**: Extremely poor practice, leads to non-reproducible builds, security risks, requires code changes and redeployments for any configuration update. Rejected.

### 3. Cloud Provider Specific Secret/Config Management

*   **Description**: Using services like AWS Secrets Manager/Parameter Store, Azure Key Vault, Google Secret Manager.
*   **Pros**: Fully managed, reduced operational overhead.
*   **Cons**: Introduces cloud vendor lock-in, which may conflict with a hybrid or multi-cloud strategy. While viable, the decision to use HashiCorp Vault aligns with a more cloud-agnostic approach for core infrastructure components.

The chosen layered approach provides a comprehensive, secure, and flexible configuration management strategy that aligns with the microservices and Kubernetes-centric architecture, balancing the benefits of specialized tools with the need for operational efficiency.
