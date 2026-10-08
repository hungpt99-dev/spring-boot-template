# Event-driven hub

- `events/conventions.md` — intents, keys, headers, consumer placement.
- `events/event-envelope.md` — the frozen contract (+ `EventEnvelopeCodec`).
- `events/versioning.md` — type evolution rules.
- `events/retry.md` — at-least-once, backoff, relay attempts.
- `events/dlq.md` — poison-message handling and replay.
- `events/idempotency.md` — dedupe patterns + duplicate tests.

One-line model: producers declare intent (best-effort vs atomic outbox),
relay delivers at-least-once, consumers are idempotent on `event-id`.
Workflow: `docs/workflows/add-domain-event.md`.
