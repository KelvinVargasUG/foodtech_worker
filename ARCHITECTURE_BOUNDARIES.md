# Hexagonal Boundaries (Ports & Adapters)

## Target Layering

- **Domain** (`src/main/java/.../domain/**`)
  - Business models and business rules only.
  - Must not depend on Spring, JPA, HTTP, Kafka/Rabbit, or transport/persistence frameworks.

- **Application** (`src/main/java/.../application/**`)
  - Use cases and port interfaces.
  - Depends on Domain + port abstractions only.
  - Must not contain framework-specific annotations or direct infra client usage.

- **Infrastructure** (`src/main/java/.../infrastructure/**`)
  - Adapters implementing ports (messaging, persistence, schedulers, REST/controller adapters, configs).
  - Contains all Spring wiring, persistence entities/repositories, and broker-specific logic.

## Confirmed Ports for Current Use Cases

- `EventPublisherPort`
  - Outbound publication of `FoodEvent`.
  - Implemented by:
    - `KafkaEventPublisher`
    - `RabbitMqEventPublisher`

- `OutboxRepositoryPort`
  - Outbox retrieval/persistence from application use cases.
  - Implemented by:
    - `OutboxRepositoryAdapter` (backed by `JpaOutboxRepository` + `OutboxEntity`)

## Dependency Direction

`Domain <- Application <- Infrastructure`

- Application use cases are instantiated in infrastructure configuration (`UseCaseConfig`).
- Broker strategy selection remains configuration-driven (`foodtech.message-broker`) in infrastructure.
- Public API adapter (`TestEventController`) delegates to application use case only.
