# Changelog

Format: Keep a Changelog. Versioning: SemVer.

## [Unreleased]

### Added

- Transactional outbox module (`app.modules.outbox.enabled`) with relay and
  startup validator.
- Engineering foundation: Checkstyle (hygiene-only ruleset), CycloneDX SBOM,
  thin `.githooks/` (pre-commit/commit-msg/pre-push), `scripts/` developer
  commands (setup/format/check/test/integration-test/verify/build/security/ci),
  `CONTRIBUTING.md`, `SECURITY.md`, PR template, `.gitattributes`,
  `.gitmessage`, `.env.example`, Gitleaks + SBOM artifacts in CI.
- Template mechanics: `scripts/init.sh` (interactive + non-interactive) with
  centralized `scripts/template-vars.sh` manifest, `scripts/check-template.sh`
  leftover validation, `CODEOWNERS`, issue templates, ADRs, Apache-2.0
  `LICENSE`, `docs/workflows/create-service.md`.
- AI-agent layer: `AGENTS.md`, `docs/modules.json`, `docs/workflows/`,
  `docs/EVENTS.md`.
- Template engine v1.1: `.template/` (`version`, `metadata.yaml`,
  `variables.yaml`), `config/service.yaml` ownership metadata, sample Kafka
  consumer (`adapter/in/messaging`), `docs/api/` (+ `openapi.yaml` skeleton),
  `docs/events/`, `docs/operations/`, `docs/architecture|security|testing|
  development/` hubs, `docs/adr/template.md` + ADR-0003 (flat deploy layout),
  `scripts/validate-template.sh` self-check; canonical `.github/
  PULL_REQUEST_TEMPLATE.md` + `.github/CODEOWNERS`; `init.sh` reads
  `variables.yaml`, writes `service.yaml`, supports `--domain`/`--owner`.

### Changed

- Deployment separation (ADR-0004, supersedes ADR-0003): app repo no longer
  answers "how and where do we deploy".

### Removed

- `k8s/app.yaml` (Deployment + Service + HPA): hardcoded private registry +
  mutable SNAPSHOT tag, company-specific `app-secrets` wiring, baked
  prod/persistence env decisions, workload-specific replica/resource/HPA
  guesses, and existing doc drift (readiness checklist claimed a PDB that was
  never in the manifest). Manifests belong in the deployment repo; the handoff
  is the new `docs/operations/kubernetes.md` contract (image, probes,
  shutdown, env interface, scaling). Dockerfile, `compose.yaml`, and CI
  `docker build` validation stay — packaging and local run are app
  responsibilities. All references updated (init/validate/template-vars,
  CODEOWNERS, `.dockerignore`, README, operations/security docs, workflows).

## [1.0.0] - TBD

- Initial company-standard template: Spring Boot 4.1.1, Java 21,
  Hexagonal-lite layout, modular persistence/redis/kafka/security/
  resilience/idempotency/docs, observability, Testcontainers, Docker/K8s.
