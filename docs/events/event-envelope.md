# Event envelope (frozen contract)

```json
{
  "eventId": "uuid",
  "type": "product.created.v1",
  "aggregateType": "Product",
  "aggregateId": "id-1",
  "occurredAt": "2026-10-08T03:00:00Z",
  "payload": { "name": "Widget", "priceCents": 999 }
}
```

Built only by `EventEnvelopeCodec` (pinned by `EventEnvelopeCodecTest`).
Never add top-level fields without an ADR; evolve inside `payload` or bump
`type` (see `versioning.md`).
