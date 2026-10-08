# 0004. No Kubernetes manifests in the application template

Date: 2026-10-08 | Status: accepted | Supersedes: 0003

## Context

`k8s/app.yaml` shipped a Deployment + Service + HPA. A template review found
it couples the app repo to platform concerns:

- Hardcoded private registry (`registry.company.local`) and a mutable
  `-SNAPSHOT` tag with `IfNotPresent` — wrong for every adopter, unsafe
  rollback story.
- Company-specific secret object (`app-secrets` + key names) — secret
  management belongs to the platform repo.
- Baked environment decisions (`SPRING_PROFILES_ACTIVE=prod`, persistence
  forced on) while app defaults keep persistence off — two sources of truth.
- Workload guesses (replicas, CPU/mem, HPA 2–8/CPU70) every service rewrites.
- Doc drift already: `production-readiness.md` claims a PDB "set in
  `k8s/app.yaml`" that was never there — proof manifests rot.
- GitOps double-truth: a deployment repo syncing the cluster plus a manifest
  in the app repo disagree about who owns the Deployment object.

## Decision

Delete `k8s/`. The app repo answers "how do we build, test, package, and run"
(Dockerfile, CI build, compose.yaml, env-var interface, probe/health
conventions). The deployment/platform repo answers "how and where do we
deploy" (manifests, per-env overlays, secrets, GitOps sync). The handoff is
`docs/operations/kubernetes.md` (image/probe/shutdown/env/scaling contract),
not a manifest.

## Consequences

- Removed: `k8s/app.yaml`; references in init.sh, template-vars.sh,
  validate-template.sh (kubectl checks become a no-manifest guard + contract
  check), `.dockerignore`, CODEOWNERS, metadata layer map, overview map,
  README/operations/security docs, create-service workflow.
- `Dockerfile`, `compose.yaml`, CI `docker build` validation stay — packaging
  and local run are app responsibilities.
- Revisit only if the company standardizes on a mono-repo where app and
  manifests version together.
