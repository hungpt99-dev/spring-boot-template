# Kubernetes deployment contract

This repo ships **no Kubernetes manifests by design** (see
`docs/adr/0004-no-k8s-manifests.md`). Manifests (Deployment, Service,
Ingress, HPA, PDB, secrets) live in the **deployment/platform repository**,
which owns clusters, environments, secrets, and GitOps sync. This page is the
contract the platform side can rely on.

## Image

- OCI image built from `./Dockerfile` (layered, JRE-only runtime).
- Listens on `8080`. Runs as non-root UID `1000` (`appuser`).
- Compatible with `readOnlyRootFilesystem: true`, `drop: [ALL]`,
  `seccompProfile: RuntimeDefault`. Needs no privilege escalation.
- Versioning: platform pins an **immutable tag or digest per environment**
  (never a mutable `-SNAPSHOT` tag with `IfNotPresent`).

## Probes (Actuator — always on)

| Probe | Path | Suggested timing | Semantics |
|---|---|---|---|
| Liveness | `/actuator/health/liveness` | delay 60s, period 10s | Restart on failure. Cheap, no downstream calls. |
| Readiness | `/actuator/health/readiness` | delay 30s, period 5s | Remove from service on failure. Includes `db`/`redis`/`kafka` indicators automatically when those modules are enabled. |

## Shutdown

- `server.shutdown: graceful` + 30s phase timeout: SIGTERM drains in-flight
  requests before context close. Give pods a `preStop` sleep (~10s) so the
  endpoint deregisters first. Details: `graceful-shutdown.md`.

## Configuration interface

The image is configured **only via environment** (see full table in
`docs/OPERATIONS.md`): `SPRING_PROFILES_ACTIVE`, `APP_MODULES_*_ENABLED`,
`DB_URL/DB_USER/DB_PASSWORD`, `REDIS_HOST/PORT`, `KAFKA_BOOTSTRAP_SERVERS`,
`JWT_ISSUER_URI`, `OTEL_EXPORTER_OTLP_ENDPOINT`, `TRACING_SAMPLING_PROBABILITY`.
Module flags and secrets are per-environment values — they belong in the
platform repo (Kustomize overlays, Helm values, or equivalent), never in app
defaults. Secrets arrive via the platform's secret manager (`secretKeyRef`
or injected env), never baked into the image or committed yaml.

## Scaling guidance

The app is stateless except the default in-memory idempotency store (see
`runbook.md`: >1 replica needs a shared `IdempotencyStore` or idempotency
off). HPA thresholds, replica counts, PDBs, resources, and namespaces are
workload- and environment-specific — the platform repo sets them per service.
Metrics for autoscaling: `http.server.requests` histogram is on;
`/actuator/prometheus` is exposed.
