# Architecture overview

Hexagonal-lite, three content layers:

```
1. Template Runtime   src/, config/service.yaml, Dockerfile, compose.yaml
                      -> what the generated service RUNS (packaging + local run;
                         deploy mapping: deploy/docker = ./Dockerfile +
                         .dockerignore, local infra = ./compose.yaml.
                         Kubernetes manifests live in the deployment repo —
                         per docs/adr/0004-no-k8s-manifests.md, handoff is
                         docs/operations/kubernetes.md, not a manifest)
2. Engineering Foundation  scripts/ (minus engine), .githooks/, .github/,
                      config/checkstyle, docs/, standards (AGENTS, CONTRIBUTING...)
3. Template Engine    .template/, scripts/{init,template-vars,check-template,validate-template}.sh
                      -> removed by init; never ships in a service
```

Module (package) rules, enforced by `ArchitectureTest`:

- `domain/model`: pure Java records. No Spring/Jakarta/Jackson.
- `app/port/in|out`: use-case + SPI interfaces. `app/service`: logic.
  `app` never imports `adapter`. No `@Transactional` in `app/service`.
- `adapter/in/*`: inbound (web, messaging consumers). `adapter/out/*`:
  outbound (persistence, publishers). `adapter.in` never imports `adapter.out`.
- `common/*`: cross-cutting (config, error, web, security, cache,
  resilience, idempotency, event, outbox, modules). No business logic.
- Tests live in the same package as the code they test (ArchUnit scans them).

Flow: `init -> transform -> validate (check-template) -> clean -> verify`.
