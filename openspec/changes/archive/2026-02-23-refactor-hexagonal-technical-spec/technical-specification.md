# Hexagonal Refactor Technical Specification

## 1. Baseline and Scope Alignment

### 1.1 Diagnostic baseline (AS-IS)
Use this checklist to classify current violations:
- Framework leakage in inner layers: `domain/**` or `application/**` importing Spring, JPA, HTTP, Kafka/Rabbit classes.
- Dependency inversion violations: `application/**` referencing infrastructure implementations.
- Business logic in adapters: controllers/adapters containing orchestration or domain decisions.
- Model leakage: DTO/entity types used as domain contracts outside mapping boundaries.
- Transaction/wiring leakage: framework concerns located outside `infrastructure/**`.

### 1.2 Measurable acceptance gates
A phase passes only if all applicable gates are green:
- **A1 Layer purity:** No forbidden imports in `domain/**`; no infrastructure implementation dependencies in `application/**`.
- **A2 Contract safety:** Existing endpoint paths, DTO shapes, media types, and status semantics unchanged.
- **A3 Test obligations:** Layer-specific tests run and pass (domain/app unit, adapter integration).
- **A4 Traceability:** Each changed behavior path is linked to at least one scenario and test evidence.

### 1.3 Scope boundaries
In scope:
- Architecture boundary correction (ports/adapters, package ownership, mapping boundaries).
- Incremental migration plan and verification gates.
- Documentation and onboarding assets.

Out of scope:
- New business features.
- Intentional API contract changes.
- Stack replacement or full rewrite.

## 2. TO-BE Architecture Contract

### 2.1 Dependency direction
Mandatory dependency flow:
- `domain` <- `application` <- `infrastructure`

Rules:
- `domain` depends on nothing framework-specific.
- `application` depends only on `domain` + port interfaces.
- `infrastructure` implements ports and contains framework wiring.

### 2.2 Canonical package ownership
- `domain/`: entities, value objects, domain services, domain exceptions, invariants.
- `application/ports/input/`: in-ports exposed to inbound adapters.
- `application/ports/output/`: out-ports required by use cases.
- `application/usecases/`: orchestration/business flows through ports.
- `application/exceptions/`: application-level exceptions.
- `infrastructure/adapters/input/`: controllers, consumers, schedulers.
- `infrastructure/adapters/output/`: persistence/external/broker adapters.
- `infrastructure/config/`: Spring beans, wiring, transactions, runtime config.

### 2.3 Forbidden dependency rules and examples
Forbidden:
- `domain/**` importing `org.springframework.*`, `jakarta.persistence.*`, transport or broker clients.
- `application/**` importing concrete adapter/repository/client classes from infrastructure.
- Inbound adapters invoking outbound adapters directly.

Allowed:
- Inbound adapter -> in-port -> use case -> out-port -> outbound adapter.

## 3. Migration Roadmap and Phase Gates

### 3.1 Phases (0-6) with entry/exit
- **Phase 0 (Baseline):** inventory violations, freeze API behavior with tests.
- **Phase 1 (Domain extraction):** move business invariants/rules to domain.
- **Phase 2 (Ports + use cases):** establish in/out ports and port-based orchestration.
- **Phase 3 (Inbound adapters):** thin controllers/consumers delegate to in-ports.
- **Phase 4 (Outbound adapters):** infrastructure implements out-ports for DB/brokers/clients.
- **Phase 5 (Alignment):** mappers, validation, and error-handling boundaries stabilized.
- **Phase 6 (Hardening):** cleanup, CI architecture checks, final docs publication.

### 3.2 Phase verification checks
Each phase must provide:
- Architecture gate evidence (imports/dependencies/ownership).
- API compatibility evidence (contract and status behavior).
- Required tests for impacted layers.

### 3.3 Rollback criteria and decisions
Rollback the phase when any applies:
- API compatibility gate fails.
- New forbidden dependency appears.
- Required tests fail with no safe hotfix path.

Decision points:
- Continue if all gates pass.
- Fix-forward only for non-contract, low-risk internal defects.
- Rollback for contract drift or unresolved boundary violations.

## 4. Conventions and Reference Flows

### 4.1 Naming and placement standards
- Input ports: `*UseCase` interfaces in `application/ports/input`.
- Output ports: `*Port` interfaces in `application/ports/output`.
- Use cases: `*Service` or verb-oriented classes in `application/usecases`.
- Adapters: suffix by technology role (`Jpa*Adapter`, `Kafka*Adapter`, `Rabbit*Adapter`, `Rest*Adapter`).
- Mappers: `*Mapper` in infrastructure mapping modules.
- Exceptions:
  - Domain exceptions in `domain/exceptions`.
  - Application exceptions in `application/exceptions`.
  - Transport/error translation in inbound adapters.
- Validation placement:
  - Domain invariants in domain.
  - Use-case preconditions in application.
  - Payload/schema validation in inbound adapters.

### 4.2 Reference execution flow
- `Controller/Consumer` receives request/event.
- Validates transport-level constraints.
- Maps DTO -> domain command/value object.
- Calls input port (`InPort`).
- Use case orchestrates domain behavior and output ports.
- Output adapter executes infrastructure side effects.
- Maps result domain -> DTO and returns response.

### 4.3 Mapping boundaries
- Domain <-> Entity mapping: infrastructure outbound persistence layer only.
- Domain <-> DTO mapping: inbound adapter layer only.
- Domain model never exposes persistence or transport annotations.

## 5. Contract Preservation and Testing Strategy

### 5.1 API compatibility checklist
For every impacted endpoint:
- Path and HTTP method unchanged.
- Request/response DTO shape unchanged.
- Media type behavior unchanged.
- Success and failure status semantics unchanged.
- Validation failure behavior preserved.

### 5.2 Layer-aligned testing obligations
- **Domain:** pure unit tests, no Spring context.
- **Application:** use-case unit tests with mocked/stubbed out-ports.
- **Infrastructure adapters:** integration tests for persistence, messaging, clients, and wiring.
- **API compatibility:** regression/contract tests for endpoints affected by a phase.

### 5.3 Evidence model and sign-off artifacts
Required per phase:
- Test execution references (unit/integration/contract outputs).
- Architecture gate result summary.
- Scenario-to-test traceability table.
- Reviewer sign-off record.

## 6. Risks, Deliverables, and Governance

### 6.1 Risk register
- Coupling leakage -> enforce forbidden-import checks and reviews.
- Contract drift -> endpoint compatibility checklist as merge gate.
- Circular dependencies -> package dependency checks before merge.
- Migration regressions -> phased rollout and rollback checkpoints.

### 6.2 Final deliverables set
Published deliverables for this change:
- `deliverables/dependency-diagram.md`
- `deliverables/target-project-tree.md`
- `deliverables/onboarding-guide.md`

### 6.3 Review/sign-off workflow and publication
Workflow:
1. Author prepares phase evidence bundle.
2. Architecture reviewer validates boundary gates.
3. API reviewer validates compatibility checklist.
4. QA validates test evidence and traceability.
5. Change owner approves sign-off and publishes updates.

Publication location:
- Working artifacts remain under `openspec/changes/refactor-hexagonal-technical-spec/`.
- After sync/archive, canonical specs are maintained under `openspec/specs/**`.
