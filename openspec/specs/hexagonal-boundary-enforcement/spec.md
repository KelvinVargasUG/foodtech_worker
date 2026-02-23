## MODIFIED Requirements

### Requirement: Domain layer is framework-independent
The system MUST keep all domain models and domain rules independent from framework and I/O concerns. Domain code SHALL NOT depend on Spring, JPA, HTTP, Kafka, RabbitMQ, or persistence/transport annotations. The architecture SHALL define a forbidden-dependency rule set and package contract that can be verified during refactor phases.

#### Scenario: Domain compiles without framework contracts
- **WHEN** domain classes are reviewed for imports and annotations
- **THEN** no framework-specific dependencies are present in the domain layer

#### Scenario: Dependency gate rejects framework leakage
- **WHEN** a change introduces a framework dependency into domain packages
- **THEN** architecture verification flags the violation before merge

### Requirement: Application layer depends on ports and domain only
Application use cases SHALL orchestrate business flow using domain models and port interfaces only. Application code MUST NOT directly call infrastructure clients, repositories, broker templates, or framework transport classes. The application layer SHALL expose in-ports and consume out-ports with explicit naming and ownership conventions.

#### Scenario: Use case orchestration through ports
- **WHEN** an application use case performs event processing
- **THEN** it invokes required outbound behavior only through defined ports

#### Scenario: Application rejects direct infrastructure dependencies
- **WHEN** application code references infrastructure implementation classes
- **THEN** architecture verification flags the dependency inversion violation

### Requirement: Infrastructure implements ports as adapters
Infrastructure components MUST implement application/domain-defined ports and contain framework-specific configuration, persistence, messaging, and scheduling details. Inbound and outbound adapters SHALL be isolated from each other except through use-case/port contracts and mapping boundaries.

#### Scenario: Adapter wiring fulfills port contracts
- **WHEN** the application context is initialized
- **THEN** concrete infrastructure adapters are bound to the corresponding port interfaces used by application use cases

#### Scenario: Adapter boundaries are respected during refactor
- **WHEN** infrastructure components are refactored
- **THEN** cross-adapter business logic is not introduced and responsibilities remain adapter-specific
