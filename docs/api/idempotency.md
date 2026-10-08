# API idempotency

Clients send `Idempotency-Key: <uuid>` on POST/PUT/PATCH/DELETE.
`IdempotencyFilter` (module `idempotency`, default ON) replays the stored
response for a repeated key instead of re-executing the handler; replays carry
`Idempotency-Key: replayed`.

Rules: keys are scoped per principal; only 2xx responses are stored
(`app.idempotency.ttl`, default 24h). Default store is in-memory (single
instance) — clustered services must provide a shared `IdempotencyStore`
(Redis/JDBC). Document the header in OpenAPI for every unsafe endpoint.
