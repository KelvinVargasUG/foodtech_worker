## MODIFIED Requirements

### Requirement: Domain behavior is unit-testable without framework runtime
Domain behavior MUST be verifiable with isolated unit tests that run without loading a Spring application context or external infrastructure. The testing standard SHALL require architecture-focused unit assertions for domain invariants and forbidden framework coupling.

#### Scenario: Domain unit test isolation
- **WHEN** domain tests are executed
- **THEN** they run without Spring context initialization and without external broker/database dependencies

#### Scenario: Domain tests validate architecture invariants
- **WHEN** domain rules and entities are refactored
- **THEN** unit tests verify business invariants and absence of framework-coupled behavior

### Requirement: Application use cases are unit-testable through ports
Application use cases SHALL be testable by mocking or stubbing port interfaces, validating orchestration and business decisions independently from infrastructure implementations. Use-case tests SHALL be traceable to specification scenarios defined for migration phases.

#### Scenario: Use case unit test with mocked ports
- **WHEN** an application use case unit test executes with mocked ports
- **THEN** it verifies expected interactions and outcomes without requiring adapter integration

#### Scenario: Use-case tests map to specification scenarios
- **WHEN** a migration phase introduces or modifies use-case orchestration
- **THEN** the associated specification scenarios are represented by use-case unit tests

### Requirement: Infrastructure adapters are verified by integration tests
Infrastructure adapters and configuration wiring MUST be covered by integration tests that validate framework wiring and external boundary behavior. Adapter integration coverage SHALL include persistence and integration-path verification relevant to the migrated phase.

#### Scenario: Adapter integration behavior verification
- **WHEN** integration tests run for messaging or persistence adapters
- **THEN** the tests verify adapter wiring and side effects against expected infrastructure interactions

#### Scenario: Phase completion enforces adapter integration evidence
- **WHEN** a migration phase affecting outbound or inbound adapters is completed
- **THEN** integration test evidence exists for the affected adapter boundaries before phase sign-off
