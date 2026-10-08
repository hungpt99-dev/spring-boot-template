# Consumer retry

Delivery is at-least-once: expect redelivery after relay restarts, rebalances,
or send timeouts. Handlers must be idempotent (dedupe on `event-id`).

- Transient failure → retry with backoff; do not block the partition longer
  than `max.poll.interval.ms` (long work goes to a worker queue).
- Outbox relay side: failed sends bump `attempts`; rows past
  `app.outbox.max-attempts` wait for operator triage (see
  `docs/operations/runbook.md`).
- Consumer side: after N attempts route to DLQ (next doc), never infinite retry.
