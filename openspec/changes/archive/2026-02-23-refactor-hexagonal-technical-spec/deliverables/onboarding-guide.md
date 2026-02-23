# Onboarding Guide: Building New Features in Hexagonal Architecture

## 1) Start from use case
- Define feature behavior in domain terms.
- Create/update input port in `application/ports/input`.
- Implement orchestration in `application/usecases`.

## 2) Define outbound needs as ports
- Add required output ports in `application/ports/output`.
- Keep use case dependent on ports only.

## 3) Implement adapters in infrastructure
- Inbound adapter: map request/event to domain command and call input port.
- Outbound adapter: implement output port and handle framework-specific concerns.

## 4) Respect mapping boundaries
- DTO <-> Domain mapping in inbound side.
- Domain <-> Entity mapping in persistence side.

## 5) Keep compatibility intact
- Preserve endpoint path/method/status/DTO semantics for existing contracts.

## 6) Testing checklist before merge
- Domain unit tests (no Spring context).
- Use-case unit tests with mocked ports.
- Adapter integration tests for changed boundaries.
- API compatibility checks for impacted endpoints.

## 7) Review checklist
- No forbidden imports in domain/application.
- No direct application dependency on infrastructure implementations.
- Evidence bundle includes scenario-to-test traceability.
