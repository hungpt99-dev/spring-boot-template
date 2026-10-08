# Operations hub

Start here, then go deep:

- `operations/production-readiness.md` — the ship checklist (gates a release).
- `operations/health-check.md` — liveness/readiness endpoints and probes.
- `operations/observability.md` — logs, metrics, traces, info.
- `operations/graceful-shutdown.md` — drain behavior and timeouts.
- `operations/failure-handling.md` — what breaks how, per module.
- `operations/runbook.md` — copy per service; outbox triage SQL inside.
- `operations/troubleshooting.md` — symptom → cause → fix table.
- `operations/kubernetes.md` — deployment-repo handoff contract (image,
  probes, shutdown, env interface, scaling). No manifests live here by design
  (ADR-0004).

Environment-variable reference (single source of truth):

| Env | Default | Notes |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | — | `local` for compose-backed dev |
| `APP_MODULES_*_ENABLED` | see README | e.g. `APP_MODULES_PERSISTENCE_ENABLED=true` |
| `DB_URL/DB_USER/DB_PASSWORD` | localhost/change-me | persistence module |
| `REDIS_HOST/REDIS_PORT` | localhost/6379 | redis module |
| `KAFKA_BOOTSTRAP_SERVERS` | localhost:9092 | kafka module |
| `JWT_ISSUER_URI` | empty | security module (required when enabled) |
| `OTEL_EXPORTER_OTLP_ENDPOINT` | empty | OTLP collector; empty = no export |
| `TRACING_SAMPLING_PROBABILITY` | 0.1 | raise briefly for incident diagnosis |
