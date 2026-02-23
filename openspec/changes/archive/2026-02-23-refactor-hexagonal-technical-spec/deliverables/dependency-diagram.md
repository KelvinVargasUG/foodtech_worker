# Dependency Diagram (Textual)

## Allowed direction

`domain <- application <- infrastructure`

## Flow diagram

```text
Inbound Adapter (REST/Consumer/Scheduler)
  -> Input Port (application)
    -> Use Case (application)
      -> Output Port (application)
        -> Outbound Adapter (infrastructure)
          -> External System (DB/Broker/HTTP)
```

## Forbidden dependency highlights

- `domain` -> `application` (forbidden)
- `domain` -> `infrastructure` (forbidden)
- `application` -> concrete infrastructure classes (forbidden)
- inbound adapter -> outbound adapter direct call (forbidden)

## Enforcement intent

Use this diagram as a code-review checklist and CI architecture rule baseline.
