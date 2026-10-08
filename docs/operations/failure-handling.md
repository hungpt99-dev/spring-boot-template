# Failure handling

- HTTP: Resilience4j retry (3x, 200ms) on reads, circuit breaker on fan-out
  (see `resilience4j.instances.products`); open breakers surface in metrics.
- Kafka produce: idempotent producer (`acks=all`, retries 3); publish errors
  are logged with aggregate id, never swallowed silently.
- Outbox relay: per-message try/catch → `markFailed` (attempts+1); batch
  continues. Stuck rows (attempts ≥ max) wait for triage — see `runbook.md`.
- DB: Hikari timeouts fail fast (3s connect); Flyway `validate-on-migrate`
  blocks boot on schema drift — fix with a migration, never `ddl-auto: update`.
