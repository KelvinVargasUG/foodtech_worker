# Deuda Técnica — Los 3 Pecados Capitales

## 1. Dominio Anémico: Lógica de negocio fuera del modelo

**Archivo:** `src/main/java/com/foodtech/kitchen/worker/foodtech_worker/application/usecases/ProcessOutboxUseCase.java` (líneas 40-73)

El modelo `OutboxEvent` es un data-bag sin comportamiento. Toda la lógica de transiciones de estado (`NEW → SENT → FAILED`), política de reintentos y marcado temporal vive en el use case:

```java
outboxEvent.setStatus("SENT");
outboxEvent.setSentAt(LocalDateTime.now());
outboxEvent.setAttempts(outboxEvent.getAttempts() + 1);
// ...
if (outboxEvent.getAttempts() >= maxAttempts) {
    outboxEvent.setStatus("FAILED");
}
```

**Viola: SRP (Single Responsibility Principle)** — `ProcessOutboxUseCase` tiene dos responsabilidades: orquestar el flujo de publicación **y** gestionar las reglas de estado/reintentos del evento. Esas reglas son lógica de dominio que pertenece a `OutboxEvent`.

---

## 2. Framework acoplado a la capa de aplicación

**Archivos:**
- `ProcessOutboxUseCase.java` → `@Service`, `@Transactional`, `@Value`
- `ProcessEventUseCase.java` → `@Service`

Los use cases dependen directamente de anotaciones de Spring:

```java
@Service
@RequiredArgsConstructor
public class ProcessOutboxUseCase {

    @Value("${foodtech.outbox.max-attempts:3}")
    private int maxAttempts;

    @Transactional
    public void processOutboxEvents() { ... }
}
```

La capa de aplicación queda soldada al framework. Si se reemplaza Spring Boot, hay que reescribir los use cases.

**Viola: DIP (Dependency Inversion Principle)** — Los módulos de alto nivel (use cases) dependen de detalles concretos del framework (anotaciones de Spring) en lugar de abstracciones. La configuración (`maxAttempts`) debería llegar por constructor; el registro como bean y la transaccionalidad deberían definirse en la capa de infraestructura.

---

## 3. Strategy seleccionada por `switch` hardcodeado + bug de `@Value` con `new`

**Archivo:** `src/main/java/com/foodtech/kitchen/worker/foodtech_worker/infrastructure/adapters/output/config/MessageBrokerConfig.java` (líneas 41-56)

La selección del broker usa un `switch` que requiere modificar la clase cada vez que se agrega un broker nuevo. Además, al instanciar los publishers con `new`, los campos anotados con `@Value` en `RabbitMqEventPublisher` nunca son inyectados por Spring, resultando en `exchange = null` y `routingKey = null` en runtime:

```java
switch (messageBroker.toLowerCase()) {
    case "kafka":
        strategy = new KafkaEventPublisher(kafkaTemplate.get());
        break;
    case "rabbitmq":
        strategy = new RabbitMqEventPublisher(rabbitTemplate); // @Value fields = null
        break;
}
```

**Viola: OCP (Open/Closed Principle)** — La clase está abierta a modificación cada vez que se agrega un broker (SNS, GCP Pub/Sub, etc.) en lugar de estar abierta a extensión. La solución es auto-discovery de implementaciones de `MessageBrokerStrategy` registradas como beans, filtrando por nombre.
