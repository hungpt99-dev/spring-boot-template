# Runbook (copy per service, link from its README)

## Triage order

1. `/actuator/health` + recent deploy (`/actuator/info` version).
2. Error budget: 5xx rate + latency p99 (`http.server.requests`).
3. Traces for a failing `correlationId`; circuit-breaker states.

## Outbox triage (Postgres)

```sql
SELECT id, type, aggregate_id, attempts, left(last_error, 200)
FROM outbox_events WHERE published_at IS NULL ORDER BY created_at;
```

Attempts ≥ `app.outbox.max-attempts`: fix broker/consumer, then
`UPDATE outbox_events SET attempts = 0 WHERE id = '...'` to requeue, or mark
published after manual reconciliation. Alert on growing unpublished counts.

## Duplicate POSTs

Normal with `Idempotency-Key` replays (`Idempotency-Key: replayed` header).
Investigate only when keys collide across clients (key generator bug).

## Scaling

Stateless except the default in-memory idempotency store: for >1 replica,
disable idempotency or provide a shared `IdempotencyStore`. Autoscaling (HPA
thresholds, replicas, PDBs) is set per environment in the deployment repo —
see `kubernetes.md`.
