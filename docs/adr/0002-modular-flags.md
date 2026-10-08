# 0002. Optional modules gated by single flags, zero-infra default

Date: 2026-10-08 | Status: accepted

## Context

Services need different infrastructure (DB, Redis, Kafka). Forcing all of it
breaks small services and slows tests; Maven profiles/optional deps add
build complexity.

## Decision

One `app.modules.<name>.enabled` flag per capability. Disabled modules are
excluded from auto-configuration by `ModularEnvironmentPostProcessor` before
Boot starts; each module ships a zero-infra fallback (in-memory/no-op) or a
fail-fast validator. Deps stay on one classpath (simpler SBOM, Dependabot).

## Consequences

Slightly larger artifact; conformance enforced by ArchUnit + `modules.json`.
New infra modules must add excludes + fallback + validator (see
`docs/workflows/enable-module.md`).
