# Event conventions

Two publishing intents (see `docs/EVENTS.md` hub):

- Best-effort: `ProductEventPublisher` port → direct Kafka. At-most-once
  relative to the DB write.
- Atomic: outbox module (`OutboxStore` + `OutboxRelay`). Row commits with the
  aggregate; delivery is at-least-once.

All events: key = aggregate id (ordering), `event-id` + `event-type` headers,
payload as primitive map. Encode only via `EventEnvelopeCodec`. Consumers live
in `adapter/in/messaging/` (sample: `ProductEventConsumer`, gated by
`app.kafka.consumer.enabled`, default off).
