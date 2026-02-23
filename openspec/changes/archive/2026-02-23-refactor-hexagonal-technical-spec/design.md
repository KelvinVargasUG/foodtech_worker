## Context

This change defines the technical blueprint to correct a partially or incorrectly implemented hexagonal architecture in a Spring Boot backend while preserving functional behavior and API contract. The current pain points include dependency direction violations (inner layers coupled to framework concerns), business logic spread across adapters, and inconsistent boundaries between domain, application, and infrastructure.

Current-state constraints and assumptions:
- Technology baseline is Java + Spring Boot.
- API style is REST.
- Persistence is JPA/Hibernate on a relational database.
- Integrations may include messaging and/or external REST clients.
- Existing behavior must remain stable during migration.
- Team needs an incremental migration plan, not a rewrite.

Stakeholders:
- Product and API consumers requiring contract stability.
- Backend engineers executing refactor phases.
- QA/DevOps ensuring release safety and rollback readiness.

## Goals / Non-Goals

**Goals:**
- Define an executable technical specification for a correct Ports & Adapters architecture in Spring Boot.
- Enforce dependency direction: `Domain <- Application <- Infrastructure`.
- Keep domain free from Spring, JPA, transport, and integration framework dependencies.
- Standardize coding conventions for ports, use cases, adapters, exceptions, mappers, and validations.
- Define an incremental migration roadmap (phase 0 to phase 6) with verification gates per phase.
- Preserve business behavior and external API contract while refactoring internals.
- Improve testability by layer (unit for domain/application, integration for adapters).

**Non-Goals:**
- Introducing new business features.
- Changing endpoint contracts, status codes, or validation semantics as part of this change.
- Replacing core platform stack (e.g., moving away from Spring Boot/JPA entirely).
- Full rewrite of all modules in one release window.

## Decisions

### Decision 1: Architecture contract and package boundaries are explicit and enforceable
Decision:
- Adopt a strict package structure with clear ownership:
  - `domain` (entities/value objects/domain services/domain exceptions)
  - `application` (use cases, in/out ports, application exceptions)
  - `infrastructure.adapters.in` (REST controllers, messaging consumers, schedulers)
  - `infrastructure.adapters.out` (JPA repositories adapters, REST clients, message producers)
  - `infrastructure.config` (Spring wiring and beans)
- Require all inbound and outbound interactions to cross a port.

Rationale:
- Explicit architecture rules reduce drift, ease onboarding, and make violations detectable.

Alternatives considered:
- Keep current structure and enforce only by convention.
  - Rejected: too weak; regressions are likely.

### Decision 2: Domain and application remain framework-agnostic
Decision:
- Domain and application code MUST NOT use Spring annotations or framework classes.
- Spring wiring and transaction boundaries are moved to infrastructure adapters/configuration.

Rationale:
- Keeps core logic portable and unit-test friendly.

Alternatives considered:
- Allow selected Spring annotations in application for convenience.
  - Rejected: creates coupling and hidden dependency inversion.

### Decision 3: Use-case orchestration is centralized in application layer
Decision:
- Controllers and inbound adapters delegate to in-ports/use cases only.
- Business decisions and orchestration move out of controllers/services tied to transport.

Rationale:
- Protects business logic from delivery technology changes.

Alternatives considered:
- Keep business orchestration in service classes under infrastructure.
  - Rejected: mixes concerns and weakens test boundaries.

### Decision 4: Persistence and integration concerns are isolated behind out-ports
Decision:
- JPA repositories, entities, REST clients, and broker clients are infrastructure-only.
- Application interacts through out-ports with adapter implementations.
- Mapping responsibilities are explicit:
  - Domain <-> Entity mappers in infrastructure
  - Domain <-> DTO mappers in inbound adapters/infrastructure mapping module

Rationale:
- Avoids leaking persistence/transport models into core logic.

Alternatives considered:
- Direct JPA repository usage from use cases.
  - Rejected: violates architecture and hampers testability.

### Decision 5: Migration is phase-gated with compatibility checks
Decision:
- Execute migration through ordered phases with acceptance checks per phase.
- No phase is considered complete without passing tests and architecture gates.

Rationale:
- Reduces delivery risk and enables rollback at controlled checkpoints.

Alternatives considered:
- Big-bang refactor.
  - Rejected: high risk and poor recoverability.

## Risks / Trade-offs

- [Risk] Hidden coupling creates regressions during extraction.
  → Mitigation: phase-by-phase migration with baseline tests and API regression suite.

- [Risk] Temporary duplication of classes/mappers during transition.
  → Mitigation: allow controlled duplication with planned cleanup tasks and explicit deprecation markers.

- [Risk] Breaking changes slip into contracts while moving controllers/adapters.
  → Mitigation: contract tests, snapshot comparisons, and compatibility checklist gates before merge.

- [Risk] Circular dependencies emerge while creating ports/adapters.
  → Mitigation: enforce package rules and architecture checks in CI; reject forbidden imports.

- [Trade-off] Additional upfront design and governance overhead.
  → Mitigation: reusable templates, naming standards, and onboarding guide to amortize cost.

## Migration Plan

### Phase 0: Preparation and baseline
- Freeze expected API behavior with integration/contract tests.
- Inventory violations (framework imports in inner layers, direct JPA from use cases, DTO/entity leaks).
- Define architecture decision record and package target tree.

### Phase 1: Domain extraction
- Move pure business concepts/rules to domain.
- Remove framework dependencies from domain.
- Introduce domain exceptions and invariants where needed.

### Phase 2: Ports and use cases
- Define in-ports/out-ports in application.
- Refactor use cases to orchestrate through ports only.
- Eliminate direct calls from use cases to repositories/clients/templates.

### Phase 3: Inbound adapters refactor
- Controllers become thin adapters mapping request/response and delegating to in-ports.
- Keep request validation semantics compatible.
- Keep HTTP status and error contract stable.

### Phase 4: Outbound adapters refactor
- Wrap JPA repositories and external clients in out-adapters implementing out-ports.
- Keep persistence/integration concerns in infrastructure.
- Introduce explicit mapper modules for model translation.

### Phase 5: Mappers, validation, and error handling alignment
- Standardize mapping boundaries (Domain<->Entity, Domain<->DTO).
- Align validation responsibility by layer:
  - Domain: invariants/business consistency
  - Application: use-case preconditions and orchestration rules
  - Inbound adapter: transport/request validation
- Align exception handling strategy by layer and mapping to API errors.

### Phase 6: Cleanup and hardening
- Remove temporary scaffolding/duplicate paths.
- Enforce standards in CI (architecture checks + tests).
- Publish architecture and onboarding documentation for new features.

Rollback strategy:
- Rollback by phase checkpoint if compatibility tests fail.
- Preserve previous release artifact and DB migration compatibility rules.

## Open Questions

- Which authentication profile is canonical for this project (JWT/OAuth2/none) and what compatibility constraints apply?
- Which integrations are currently mandatory in production (Kafka/Rabbit/REST clients) for adapter test prioritization?
- Should architecture enforcement include an automated rule set (e.g., ArchUnit) in this same change or a follow-up?
- What minimum coverage thresholds by layer should be enforced in CI?
