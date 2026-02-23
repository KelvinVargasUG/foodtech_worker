## Why

The current codebase claims a hexagonal architecture, but key boundaries are violated (business logic mixed in controllers/services, direct JPA usage from use cases, and framework coupling in inner layers), which increases change risk and maintenance cost. We need an executable technical refactor specification now to guide a safe, incremental migration to a correct Ports & Adapters model without altering business behavior.

## What Changes

- Create a complete technical refactor specification for Spring Boot focused on correcting an improperly implemented hexagonal architecture.
- Define objective diagnostic criteria for “mal hecha” vs “hexagonal correcta” to evaluate current state and validate progress.
- Define the TO-BE architecture contract with strict dependency rules:
  - `domain` independent from Spring/JPA/Web/integration frameworks
  - `application` with use cases and ports only
  - `infrastructure` with inbound/outbound adapters and framework wiring
- Define a phased migration roadmap (phase 0 to phase 6) for incremental refactor without full rewrite.
- Define coding standards for naming, ports, adapters, use cases, exceptions, and validation placement by layer.
- Define minimal reference contracts/flows (controller → in-port → use case → out-port → adapter), plus mapper boundaries.
- Define test strategy by layer (unit for domain/use cases, integration for adapters, API compatibility checks).
- Define risks/mitigations and an acceptance checklist with clear architecture gates.
- Define final delivery artifacts (dependency diagram text, final project tree, team onboarding guide).
- Preserve functional behavior and external API contract as a non-breaking constraint.

## Capabilities

### New Capabilities
- `hexagonal-refactor-specification`: A complete, executable technical specification that standardizes diagnosis, TO-BE architecture, migration phases, coding standards, test strategy, risks, acceptance gates, and delivery artifacts for refactoring to correct hexagonal architecture.

### Modified Capabilities
- `hexagonal-boundary-enforcement`: Extend requirements to include enforceable package contracts, anti-corruption rules for adapters, and architecture acceptance gates used during phased migration.
- `api-contract-preservation-during-refactor`: Extend requirements to include explicit regression checks and compatibility controls during each migration phase.
- `layered-testability`: Extend requirements to include phase-based testing obligations, traceability from scenarios to test suites, and minimum coverage expectations by layer.

## Impact

- Affected scope:
  - `openspec/changes/refactor-hexagonal-technical-spec/**` (proposal/specs/design/tasks for the new change)
  - Potentially updates to main specs under `openspec/specs/**` after sync/merge
- Affected implementation areas (when executed):
  - `src/main/java/**` architecture boundaries and adapter wiring
  - `src/test/java/**` unit/integration contract validations
  - project architecture documentation and onboarding material
- API/runtime impact target:
  - No intended behavior change in business logic or external API contracts.
- Dependencies/systems impacted by specification scope:
  - Spring Boot layer boundaries, JPA adapters, REST adapters, messaging adapters (Kafka/Rabbit), mapping strategy, and validation/error handling conventions.
