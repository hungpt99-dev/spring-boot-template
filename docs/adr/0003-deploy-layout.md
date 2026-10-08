# 0003. Flat deploy layout (Dockerfile, k8s/, compose.yaml at root)

> SUPERSEDED by [0004](0004-no-k8s-manifests.md) — `k8s/` was removed; the
> Dockerfile/compose.yaml flat-layout rationale below still stands.

Date: 2026-10-08 | Status: superseded by 0004

## Context

A proposed structure groups deployment files under `deploy/docker/` and
`deploy/kubernetes/`. Spring Boot convention (start.spring.io, Boot guides)
keeps `Dockerfile` at the root; every reference in this repo (CI
`docker build .`, `k8s/app.yaml` kubectl path, init.sh transforms,
validate-template.sh checks, README quickstart) assumes the flat layout.

## Decision

Keep `Dockerfile`, `k8s/app.yaml`, and `compose.yaml` at the root. Mapping to
the proposed structure: `deploy/docker/` = `./Dockerfile` + `.dockerignore`,
`deploy/kubernetes/` = `./k8s/`, local infra = `./compose.yaml`. The
`docs/architecture/overview.md` layer map records this explicitly.

## Consequences

No file churn, no broken references, no init/validate rewrite. Revisit only
if a service outgrows one deployment target (then add `deploy/` overlays
per environment instead of moving the happy path).
