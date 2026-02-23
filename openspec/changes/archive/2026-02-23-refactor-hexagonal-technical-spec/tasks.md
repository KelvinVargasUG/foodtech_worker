## 1. Baseline and Scope Alignment

- [x] 1.1 Consolidate current-state diagnostics for hexagonal violations (framework leakage, dependency inversion, logic in adapters)
- [x] 1.2 Define measurable acceptance gates for architecture compliance and contract preservation
- [x] 1.3 Capture in-scope vs out-of-scope boundaries for the refactor initiative

## 2. TO-BE Architecture Contract

- [x] 2.1 Document final dependency direction contract: Domain <- Application <- Infrastructure
- [x] 2.2 Define canonical package structure and ownership for domain, application ports/use cases, and infrastructure adapters/config
- [x] 2.3 Specify forbidden dependency rules and violation examples per layer

## 3. Migration Roadmap and Phase Gates

- [x] 3.1 Define phase-by-phase roadmap (0 through 6) with entry/exit criteria
- [x] 3.2 Attach compatibility and architecture verification checks to each migration phase
- [x] 3.3 Define rollback criteria and decision points for failed phase gates

## 4. Conventions and Reference Flows

- [x] 4.1 Standardize naming/placement conventions for ports, use cases, adapters, mappers, validations, and exceptions
- [x] 4.2 Document reference execution flow: Controller -> InPort -> UseCase -> OutPort -> Adapter
- [x] 4.3 Document mapping boundaries and ownership for Domain <-> Entity and Domain <-> DTO transformations

## 5. Contract Preservation and Testing Strategy

- [x] 5.1 Define API compatibility checklist for endpoints, DTO shapes, media types, and status semantics
- [x] 5.2 Define layer-aligned test obligations (domain/application unit tests, adapter integration tests)
- [x] 5.3 Define phase evidence requirements linking spec scenarios to tests and sign-off artifacts

## 6. Risks, Deliverables, and Governance

- [x] 6.1 Create risk register with mitigations for coupling leakage, contract drift, circular dependencies, and migration regressions
- [x] 6.2 Produce final deliverables set (dependency diagram, target project tree, onboarding guide)
- [x] 6.3 Define review/sign-off workflow and publication location for the technical specification
