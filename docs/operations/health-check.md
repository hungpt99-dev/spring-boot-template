# Health checks

- Liveness: `GET /actuator/health/liveness` — restart on failure. Cheap, no
  downstream calls.
- Readiness: `GET /actuator/health/readiness` — includes `db`, `redis`,
  `kafka` indicators automatically when those modules are enabled. K8s removes
  the pod from service on failure without restarting.
- Platform wiring (suggested): liveness delay 60s / period 10s, readiness
  delay 30s / period 5s — set in the deployment repo, see `kubernetes.md`.
- `management.endpoint.health.show-details`: `when-authorized` in prod,
  `always` in `local` profile.
