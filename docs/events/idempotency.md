# Consumer idempotency

Because delivery is at-least-once, every consumer must tolerate duplicates:

1. Dedupe on the `event-id` header (processed-ids table with the same
   transaction as the handling side effects, or a natural business key).
2. Prefer natural idempotency: upserts keyed by aggregate id over
   insert-if-absent + error.
3. Make side effects (emails, charges) guarded by the same dedupe record —
   never act before recording.
4. Outbox relay itself is safe to run on N replicas (`SKIP LOCKED` claiming).

Test duplicates explicitly: deliver the same envelope twice, assert one effect.
