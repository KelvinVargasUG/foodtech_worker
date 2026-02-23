# Foodtech Worker — Project Context

## Project Overview

`foodtech_worker` is a Spring Boot worker service that processes domain events and publishes them to a message broker.

Core behavior:
- Processes events in application use cases.
- Uses an Outbox pattern for reliable event publishing.
- Supports runtime broker strategy selection (`kafka` or `rabbitmq`) via configuration.

## Tech Stack

- Language: Java 17
- Framework: Spring Boot 3.5.11
- Build: Gradle
- Data access: Spring Data JPA + PostgreSQL
- Messaging: Spring AMQP (RabbitMQ) and Spring Kafka
- Validation: Spring Validation
- Utilities: Lombok
- Document/PDF dependency present: iText 7 (`itext7-core`)
- Testing: JUnit 5, Mockito, AssertJ, Awaitility, Spring Boot Test

## Runtime and Infrastructure

- Application name: `foodtech_worker`
- Default app port: `8081`
- Main DB: PostgreSQL (`foodtech_db`)
- Broker strategy switch: `foodtech.message-broker`
  - `kafka` (current default in `application.properties`)
  - `rabbitmq`
- Local infra via `compose.yml`:
  - Kafka (`9092`) + Kafka UI (`8088`)
  - RabbitMQ (`5672`, management `15672`)
  - PostgreSQL (`5432`)

## Architecture and Package Conventions

The codebase follows a hexagonal/clean-style layering:

- `domain/model`: domain models (`FoodEvent`, `OutboxEvent`)
- `application/usecases`: orchestration and business workflow (`ProcessEventUseCase`, `ProcessOutboxUseCase`)
- `application/ports/output`: outbound ports (interfaces)
- `infrastructure/adapters/output`: broker implementations and strategy/factory wiring
- `infrastructure/persistence`: JPA entity, repository, and adapter mapping to domain port
- `infrastructure/adapters/input`: scheduling trigger (`OutboxScheduler`)
- `infrastructure/config`: Spring infra configuration (`RabbitMqConfig`, etc.)

Dependency direction convention:
- Domain has no framework dependencies.
- Application depends on domain + ports.
- Infrastructure implements ports and wires Spring-specific integrations.

## Domain and Message Conventions

- Outbox entity/table: `outbox_event`
- Outbox status values: `NEW`, `SENT`, `FAILED`
- Retry policy is attempt-based (`foodtech.outbox.max-attempts`)
- Scheduler trigger is configurable (`foodtech.outbox.scheduler-rate`)
- Event payload is stored as JSONB in Postgres (`payload` column)

## Coding Conventions

- Use constructor injection; prefer `@RequiredArgsConstructor`.
- Keep use case classes in `application/usecases` and avoid broker-specific logic there.
- Keep broker-specific behavior in strategy implementations (`KafkaEventPublisher`, `RabbitMqEventPublisher`).
- Use Lombok for boilerplate on domain/entity models (`@Data`, `@Builder`, etc.).
- Prefer explicit, contextual logs (`[Outbox]`, `[Scheduler]`) for async flows.
- Keep configuration-driven behavior in Spring `@Configuration` classes.
- Naming style:
  - Use `*UseCase` for application services.
  - Use `*Port` for interfaces.
  - Use `*Adapter` for infrastructure implementations.

## Testing Conventions

- Unit tests:
  - JUnit 5 + Mockito for use cases and adapters.
  - Verify interactions through ports/broker templates.
- Integration tests:
  - `@SpringBootTest` for wiring and outbox flow.
  - Awaitility for asynchronous scheduler assertions.
- Prefer testing behavior at boundaries (port calls, persisted status transitions, publish side effects).

## Useful Commands

- Run tests: `./gradlew test`
- Run app: `./gradlew bootRun`

## OpenSpec Authoring Guidance for This Repo

When generating OpenSpec artifacts for this project:

- Anchor requirements in use-case behavior and outbox reliability.
- Specify whether change affects Kafka, RabbitMQ, or broker-agnostic strategy logic.
- If persistence changes are needed, explicitly include `OutboxEntity`/repository impact.
- Prefer small, incremental changes that preserve existing package boundaries.
- Include test impact in tasks (unit + integration when async flow is touched).
