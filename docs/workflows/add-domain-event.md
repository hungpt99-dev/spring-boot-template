# Workflow: add a domain event

1. Decide intent: best-effort notification → direct Kafka publish via the
   `ProductEventPublisher` port. Must survive DB-write-then-crash → outbox.
2. Event type string: `<aggregate>.<action>.v1` (e.g. `product.created.v1`).
   Version the string on breaking payload changes; never rename in place.
3. Payload: `Map<String, ?>` of primitives at the call site. Encode ONLY via
   `EventEnvelopeCodec` (envelope `{eventId,type,aggregateType,aggregateId,
   occurredAt,payload}` is frozen — see `EventEnvelopeCodecTest`).
4. Outbox path: the JPA adapter appends the row in the SAME `@Transactional`
   method (follow `ProductPersistenceAdapter.save`); the relay delivers.
   Consumer contract: key = aggregate id (ordering), dedupe on `event-id`
   header, handler idempotent (see `docs/EVENTS.md`).
5. Tests: codec-level assertion of the new payload + relay/markPublished unit
   test if relay behavior changes. No broker needed (mock `KafkaTemplate`).
6. Run `mvn spring-javaformat:apply`, then `mvn verify`.
