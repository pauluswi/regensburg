# ADR-006: Java Version 17 vs 21

## Status

Proposed

## Context

The Core Banking Middleware Modernization project requires a modern, performant, and well-supported Java Virtual Machine (JVM) and language version for developing new microservices. The existing legacy middleware may be running on older Java versions, but new development should leverage the latest Long-Term Support (LTS) releases to benefit from performance improvements, new language features, and extended support.

Currently, Java 17 is the most widely adopted LTS release, having been released in September 2021. Java 21 is the latest LTS release, published in September 2023, offering further enhancements. The decision needs to be made between these two LTS versions for the project's new codebase.

## Decision

We choose **Java 21 LTS** as the primary Java version for all new microservices development within the Core Banking Middleware Modernization project.

## Consequences

### Positive

*   **Access to Latest Features**: Java 21 includes new language features (e.g., Pattern Matching for switch, Record Patterns, Virtual Threads (Project Loom) in preview, Sequenced Collections) that can lead to more concise, readable, and efficient code.
*   **Performance Improvements**: Each new Java release typically brings significant performance enhancements to the JVM, garbage collectors, and core libraries. Java 21 builds upon the improvements in Java 17, offering better throughput and lower latency.
*   **Long-Term Support**: As an LTS release, Java 21 will receive extended support and critical updates from Oracle and the OpenJDK community, ensuring stability and security for the project's lifespan.
*   **Future-Proofing**: Adopting the latest LTS version positions the project well for future advancements and reduces the technical debt associated with upgrading from older versions later.
*   **Virtual Threads (Preview)**: While in preview, the availability of Virtual Threads in Java 21 offers a significant potential for improving the scalability and efficiency of I/O-bound services, which are common in middleware. This allows for exploring highly concurrent designs with simpler programming models.

### Negative

*   **Maturity of Ecosystem**: While Java 21 is an LTS, some third-party libraries, frameworks, and tooling might have slightly less mature support or require minor updates compared to Java 17, which has been stable for longer. This risk is generally low for major frameworks like Spring Boot.
*   **Learning Curve**: Developers unfamiliar with Java 17 or 21 features will require some time to adapt and learn the new constructs.
*   **Build Tooling Compatibility**: Ensure that build tools (e.g., Maven, Gradle) and CI/CD pipelines are compatible with Java 21. This is generally a minor configuration update.
*   **Container Image Availability**: Ensure that base container images (e.g., for Docker/Kubernetes) are readily available and optimized for Java 21. This is usually not an issue for popular distributions.

## Alternatives Considered

### Java 17 LTS

*   **Pros**: Highly mature ecosystem, widely adopted, stable.
*   **Cons**: Lacks the latest language features and performance optimizations available in Java 21. Would require another major upgrade sooner to benefit from newer LTS features.

### Java 8 / Java 11

*   **Pros**: Potentially easier migration from very old legacy systems.
*   **Cons**: Significantly older, missing critical performance improvements, security updates, and modern language features. Not suitable for new development due to increased technical debt and reduced developer productivity.
