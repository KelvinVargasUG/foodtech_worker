## Context

This service currently uses Spring Boot with an outbox flow and pluggable messaging strategy (Kafka or RabbitMQ), and already contains partial layering (`domain`, `application`, `infrastructure`).

However, dependency boundaries are inconsistent in places:
- Framework annotations and configuration concerns are mixed with components that should remain pure application/domain.
- Architectural contracts (what can depend on what) are not explicit.
- Tests exist, but are not systematically aligned to architectural layers.

The refactor must enforce Hexagonal Architecture without changing external behavior:
- Keep the same HTTP endpoints, request/response DTOs, status codes, and validation behavior.
- Keep equivalent broker publishing behavior and outbox processing semantics.

Stakeholders:
- API consumers that rely on current contracts.
- Internal maintainers extending use cases and adapters.
- QA/integration pipeline relying on current runtime behavior.

Constraints:
- No breaking API changes.
- Preserve current data model semantics and message flow behavior.
- Keep compatibility with existing Spring Boot runtime and configuration properties.

## Goals / Non-Goals

**Goals:**
- Enforce strict separation of Domain, Application, and Infrastructure.
- Ensure Domain contains no dependencies on Spring, persistence, HTTP, messaging, or other framework APIs.
- Ensure Application depends on ports/interfaces and domain models only.
- Keep Infrastructure as the only layer implementing ports and binding framework/IO concerns.
- Preserve external API and validation behavior exactly.
- Improve testability by making domain/application unit-test-first and adapter integration-test-focused.

**Non-Goals:**
- Introducing new business capabilities or endpoint behavior.
- Changing request/response schemas, status codes, or validation semantics.
- Replacing current broker technologies (Kafka/RabbitMQ) or outbox strategy.
- Changing deployment topology or introducing new external infrastructure.

## Decisions

### 1) Adopt strict dependency direction with explicit layer boundaries
Decision:
- Dependency direction is enforced as `Domain <- Application <- Infrastructure`.
- Domain has zero framework dependencies.
- Application exposes/consumes ports as interfaces; Infrastructure provides implementations.

Rationale:
- This is the core hexagonal contract and enables isolated testing and safer refactoring.

Alternatives considered:
- Keep current partial layering and only rename packages.
  - Rejected: does not reduce coupling or improve testability materially.
- Introduce a modular multi-module Gradle split now.
  - Rejected for now: higher migration cost/risk than needed for this refactor.

### 2) Keep API contract and validation behavior as compatibility constraints
Decision:
- Existing controllers/routes and DTO contracts remain unchanged.
- Validation annotations and error semantics remain unchanged from current behavior.

Rationale:
- Primary acceptance criterion is architectural refactor with zero external API behavior change.

Alternatives considered:
- Refactor API DTOs in parallel for “cleanliness”.
  - Rejected: creates avoidable behavior-change risk.

### 3) Preserve message broker strategy behavior behind outbound ports
Decision:
- Broker-specific publishing stays in infrastructure adapters.
- Application uses only outbound publishing port abstractions.
- Existing runtime switch (`foodtech.message-broker`) remains the selector.

Rationale:
- Keeps business flow broker-agnostic while preserving current operational flexibility.

Alternatives considered:
- Collapse to single broker during refactor.
  - Rejected: changes supported behavior and operational expectations.

### 4) Keep persistence mapping confined to infrastructure
Decision:
- JPA entities/repositories remain infrastructure-only.
- Mapping between persistence entities and domain models is handled in repository adapters.

Rationale:
- Avoids leaking persistence concerns into domain/application.

Alternatives considered:
- Use domain entities directly as JPA entities.
  - Rejected: introduces framework coupling into domain layer.

### 5) Align test strategy to architecture boundaries
Decision:
- Domain/Application: focused unit tests with no Spring context.
- Infrastructure adapters/config/wiring: integration tests with Spring context and mocks/test infrastructure where needed.
- Add contract-style checks to ensure API behavior remains unchanged.

Rationale:
- Fast feedback for logic changes and confidence for boundary wiring and integration behavior.

Alternatives considered:
- Keep mostly integration-heavy testing only.
  - Rejected: slower and less precise at catching architectural regressions.

## Risks / Trade-offs

- [Risk] Hidden behavior coupling during class/package moves may alter runtime wiring.
  → Mitigation: incremental refactor steps with frequent integration test runs and bean wiring verification.

- [Risk] API behavior may drift unintentionally (status/validation/DTO shape).
  → Mitigation: preserve controller contracts as-is and add/retain endpoint-level assertions in tests.

- [Risk] Broker adapter changes may alter publish semantics or error handling.
  → Mitigation: retain existing strategy selection behavior and adapter integration tests for Kafka/Rabbit paths.

- [Risk] Refactor introduces temporary duplication while migrating interfaces/adapters.
  → Mitigation: short-lived transition branches and cleanup tasks with architecture checklist before completion.

- [Trade-off] No multi-module split in this iteration.
  → Mitigation: enforce boundaries by package conventions + tests now; evaluate physical module split later if needed.

## Migration Plan

1. Establish target package contracts and port definitions (without behavior change).
2. Refactor application use cases to depend strictly on ports/domain models.
3. Move/adjust infrastructure adapters to implement ports and keep framework concerns isolated.
4. Keep controllers and DTOs stable; update wiring only.
5. Update/add unit tests for domain/application and integration tests for adapters/wiring.
6. Run full test suite and regression checks for API and outbox/broker behavior.
7. Remove temporary compatibility code and finalize boundary checks.

Deployment strategy:
- In-place deployment with no API contract change.
- Configuration keys remain backward-compatible.

Rollback strategy:
- Revert to previous release artifact if regression is detected.
- No data migration required for this change; persistence schema behavior remains compatible.

## Open Questions

- Do we want architecture boundary enforcement via static analysis tooling (e.g., ArchUnit) in this change or defer to a follow-up?
- Should a future iteration split layers into separate Gradle modules for compile-time boundary enforcement?
- Are there additional API regression fixtures (beyond current tests) required by downstream consumers?
