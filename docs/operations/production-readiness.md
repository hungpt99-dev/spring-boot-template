# Production readiness checklist

A service is production-ready when ALL hold:

- [ ] Boots with zero-infra defaults; each enabled module points at real infra
- [ ] `/actuator/health/liveness` + `/readiness` wired as K8s probes
- [ ] Prometheus scraping `/actuator/prometheus`; `outbox_unpublished`-style
      custom metrics added per module in use
- [ ] OTLP endpoint set (`OTEL_EXPORTER_OTLP_ENDPOINT`) or explicitly waived
- [ ] Resource requests/limits + HPA + PDB set per environment in the
      deployment repo (see `kubernetes.md` for what the image supports)
- [ ] Secrets via secret manager/env, never yaml (`secret-scan` clean)
- [ ] Security module on for internet-facing paths (or gateway-terminated auth documented)
- [ ] Flyway migrations backward-compatible; `ddl-auto: validate`
- [ ] Runbook + dashboards linked from service README
- [ ] `sh scripts/verify.sh` green; SBOM stored per release
