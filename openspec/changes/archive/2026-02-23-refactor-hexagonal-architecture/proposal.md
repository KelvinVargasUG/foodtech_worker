## Why

The current service already has partial layering, but framework and messaging concerns are still mixed with business flow in ways that make architecture boundaries inconsistent and harder to evolve safely. We need a clear Hexagonal Architecture (Ports & Adapters) refactor now to improve maintainability and testability while guaranteeing zero external API behavior change.

## What Changes

- Reorganize and enforce architecture boundaries into:
  - Domain (pure business model/rules, no Spring/JPA/HTTP/messaging dependencies)
  - Application (use cases + ports for inbound/outbound interactions)
  - Infrastructure (Spring configuration, adapters, persistence, broker integrations)
- Define/normalize ports (interfaces) in Domain/Application layers and move concrete implementations to Infrastructure adapters.
- Refactor use cases to depend only on ports and domain objects.
- Keep HTTP/API behavior exactly the same:
  - Same endpoints and routes
  - Same request/response DTO contracts
  - Same HTTP status codes
  - Same validations and validation error semantics
- Keep messaging behavior functionally equivalent (Kafka/Rabbit strategy selection and event publishing behavior preserved).
- Improve test coverage and structure:
  - Add/adjust unit tests for domain and application use cases
  - Add/adjust integration tests for infrastructure adapters and wiring

## Capabilities

### New Capabilities
- `hexagonal-boundary-enforcement`: The service enforces strict Domain/Application/Infrastructure boundaries with dependency direction from inner layers to outer layers only.
- `api-contract-preservation-during-refactor`: Refactoring internal architecture must not change public API contracts, status codes, endpoint signatures, or validation behavior.
- `layered-testability`: The codebase supports isolated unit testing for domain/application logic and integration testing for infrastructure adapters.

### Modified Capabilities
<!-- None. There are no existing baseline specs in openspec/specs yet. -->

## Impact

- Affected code areas:
  - `src/main/java/.../domain/**`
  - `src/main/java/.../application/**`
  - `src/main/java/.../infrastructure/**`
  - Broker configuration/adapter wiring (`MessageBrokerConfig`, Kafka/Rabbit adapters)
  - Outbox processing and persistence adapter boundaries
- Public API impact:
  - No externally observable API behavior changes expected.
- Testing impact:
  - Existing tests will be reorganized/updated to align with new boundaries.
  - New unit tests for domain/application and integration tests for adapters will be added or adjusted.
- Delivery risk:
  - Medium internal refactor risk, mitigated by API contract preservation checks and test coverage expansion.
