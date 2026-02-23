## 1. Architecture Baseline and Guardrails

- [x] 1.1 Document and lock target package boundaries for Domain, Application, and Infrastructure in the codebase structure.
- [x] 1.2 Identify all framework dependencies currently leaking into Domain/Application and list required moves/refactors.
- [x] 1.3 Define/confirm port interfaces required by use cases (event publishing, outbox persistence, and other outbound concerns).

## 2. Domain Refactor (Framework Independence)

- [x] 2.1 Refactor Domain models/rules to remove Spring, JPA, HTTP, and messaging dependencies.
- [x] 2.2 Ensure Domain classes expose only business-centric data/behavior and remain annotation-free from infrastructure frameworks.
- [x] 2.3 Add/adjust unit tests that validate Domain behavior in isolation (no Spring context).

## 3. Application Refactor (Use Cases + Ports)

- [x] 3.1 Refactor use cases to depend only on Domain types and port interfaces.
- [x] 3.2 Remove direct infrastructure/framework calls from Application layer and route all outbound interactions through ports.
- [x] 3.3 Add/adjust unit tests for use cases using mocked/stubbed ports to verify orchestration and outcomes.

## 4. Infrastructure Adapters and Wiring

- [x] 4.1 Refactor persistence adapters to implement repository ports and keep JPA entities/repositories confined to Infrastructure.
- [x] 4.2 Refactor broker adapters (Kafka/RabbitMQ) to implement publishing ports while preserving strategy selection behavior.
- [x] 4.3 Update Spring configuration/wiring so concrete adapters satisfy port contracts without changing external behavior.
- [x] 4.4 Add/adjust integration tests for adapter wiring and side effects (persistence + messaging paths).

## 5. API Contract Preservation

- [x] 5.1 Keep existing controllers, routes, DTOs, status codes, and validation semantics unchanged during refactor.
- [x] 5.2 Add/adjust API-focused tests to assert endpoint compatibility for valid and invalid request flows.
- [x] 5.3 Verify no public contract drift by comparing pre/post behavior for existing endpoints.

## 6. Regression Verification and Completion

- [x] 6.1 Run full test suite and fix regressions introduced by boundary refactoring.
- [x] 6.2 Execute outbox flow regression checks to confirm status transitions and publish behavior remain equivalent.
- [x] 6.3 Perform final architecture review to confirm dependency direction: Domain <- Application <- Infrastructure.
- [x] 6.4 Remove temporary migration scaffolding/duplication and finalize refactor cleanup.
