# Target Project Tree (Hexagonal)

```text
src/main/java/com/foodtech/kitchen/worker/foodtech_worker/
  domain/
    model/
    service/
    exception/
  application/
    ports/
      input/
      output/
    usecases/
    exception/
  infrastructure/
    adapters/
      input/
        rest/
        messaging/
        scheduler/
      output/
        persistence/
        messaging/
        client/
        mapper/
    config/
```

## Ownership summary

- `domain`: pure business model and invariants.
- `application`: orchestration and port contracts.
- `infrastructure`: framework integrations and adapter implementations.
