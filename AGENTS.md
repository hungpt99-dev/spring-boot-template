# AGENTS.md — instructions for AI agents (and humans) working in this repo

Vendor-neutral. Follow these rules for every change. Prefer editing existing
code over adding new code. Keep the default path zero-infra.

## 1. Where code belongs

```
com.company.template
├── domain/model/          # Pure Java records. NO Spring, NO Jakarta, NO Jackson.
├── app/port/in/           # Use-case interfaces (e.g. ProductService).
├── app/port/out/          # SPI interfaces ONLY (repository, publisher, OutboxStore).
├── app/service/           # Use-case implementations. No @Transactional here.
├── adapter/in/web/        # Controllers, request/response DTOs, manual mappers.
├── adapter/out/persistence/ # JPA entities, Spring Data repos, port adapters.
├── adapter/out/messaging/ # Kafka / no-op publishers, publisher selection config.
└── common/                # Cross-cutting: config, error, web, security, cache,
                           # resilience, idempotency, event, outbox, modules.
```

ArchUnit (`ArchitectureTest`) enforces: `domain` is framework-free;
`app` never imports `adapter`; `adapter.in` never imports `adapter.out`.
Keep test classes in the same package as the code they test (ArchUnit scans tests too).

## 2. Patterns: use vs forbidden

USE: constructor injection; records for domain/DTOs; `Clock` (never
`Instant.now()`); RFC 9457 `ProblemDetail` via `GlobalExceptionHandler` with a
`TEMPLATE_*` code; `EventEnvelopeCodec` for ALL event JSON (never hand-roll);
`ObjectProvider` for optional module beans; repeatable
`@ConditionalOnProperty` for AND-semantics; `ModularEnvironmentPostProcessor`
excludes for new infra modules (see §4).

FORBIDDEN: Lombok; MapStruct/other mappers (`ProductMapper` style only); field
injection; `@Transactional` in `app/service` (transactions live in outbound
adapters); new auto-config exclusions without updating
`ModularEnvironmentPostProcessor` + its test; catching exceptions to return
null; logging secrets/PII; editing released Flyway migrations.

## 3. Reuse catalog (check first, in order)

`EventEnvelopeCodec` → `GlobalExceptionHandler` + `TEMPLATE_*` codes →
`CorrelationIdFilter` (MDC `correlationId`) → `CommonConfig.clock()` →
`InMemoryProductRepository` (default persistence) →
`NoOpProductEventPublisher` (default events) → `OutboxStore`/`OutboxRelay`
(atomic publish) → `ResilientProductService` pattern (decorator, not logic) →
`IdempotencyStore` SPI → `ModuleProperties` flags.

## 4. Modules (single knob each: `app.modules.<name>.enabled`)

Registry: `docs/modules.json` (machine-readable). Defaults: everything OFF
except `idempotency` + `docs`. Enabling an infra module means:
1. Flip the flag (yaml or `APP_MODULES_<NAME>_ENABLED=true`).
2. If it brings a Boot auto-config, add its FQNs to
   `ModularEnvironmentPostProcessor` (Boot 4 package names!) + extend
   `ModularEnvironmentPostProcessorTest`.
3. Provide a zero-infra fallback (in-memory / no-op) OR a fail-fast
   `ApplicationRunner` validator like `OutboxModuleValidator`.
4. Document in `docs/modules.json` + `README.md` + metadata JSON.

## 5. Events (see docs/EVENTS.md)

Direct Kafka = at-most-once intent. Need atomicity with the DB write → outbox
(requires persistence + kafka, validated at startup). Consumers are
at-least-once: key by aggregate id (ordering), dedupe on `event-id` header,
keep handlers idempotent. New event type? Add `*.created.v1`-style constant +
payload map at the call site, reuse the codec. No new envelope format, ever.

## 6. How to test

Pyramid (see docs/workflows/): pure unit (no context) → web slice
(`@WebMvcTest`, new Boot 4 package `...boot.webmvc.test.autoconfigure...`,
`@MockitoBean` is now a Spring Framework annotation) → ArchUnit → IT
(`*IT`, `@ServiceConnection`, `@Testcontainers(disabledWithoutDocker=true)`,
run by failsafe). Inject fixed `Clock`s. Name tests `<Thing>Test` (surefire)
or `<Thing>IT` (failsafe, needs Docker).

## 7. Quality gates (`mvn verify` or `sh scripts/verify.sh` must pass)

1. `spring-javaformat:validate` (run `sh scripts/format.sh` to fix; tabs).
2. Checkstyle (`config/checkstyle/checkstyle.xml`, hygiene only — formatting
   belongs to the formatter; run via `sh scripts/check.sh`, same as pre-commit).
3. All tests (unit + slice + ArchUnit; ITs run only with Docker).
4. JaCoCo line gate 70% (live-infra wiring in `persistence`/`cache`/
   `security` packages is excluded; docker ITs cover it in CI).
5. Spring Boot build succeeds with default (zero-infra) properties.
6. Secrets: `sh scripts/secret-scan.sh` clean (pre-commit runs it on staged files).

Developer commands: `sh scripts/<setup|format|check|test|integration-test|
verify|build|security|ci>.sh` plus `sh scripts/validate-template.sh`
(template self-check: metadata, hooks, build, k8s, OpenAPI, secrets).
See `CONTRIBUTING.md`; PowerShell: same Maven goals via `./mvnw`.
Git hooks (`.githooks/`, installed by setup): pre-commit = secret-scan +
check; commit-msg = Conventional Commits; pre-push = unit tests.
Hooks are thin — logic lives in `scripts/`.

Workflows with steps: `docs/workflows/add-endpoint.md`,
`docs/workflows/add-domain-event.md`, `docs/workflows/enable-module.md`,
`docs/workflows/create-service.md`.
Conventions detail: `README.md`. Ops: `docs/OPERATIONS.md`.
Upgrades: `docs/UPGRADE.md` (includes Boot 4 deltas).

## 8. Template rule (this repo is a TEMPLATE)

Never hand-edit template identifiers (`microservice-template`,
`com.company.template`, `TemplateApplication`). To create a service, run
`sh scripts/init.sh` (see `docs/workflows/create-service.md`); it applies
the manifest in `scripts/template-vars.sh`, drops template-only tooling, and
verifies the result. Validate generated trees with
`sh scripts/check-template.sh <dir>`.

Template engine files: `.template/` (`version`, `metadata.yaml`,
`variables.yaml` — flag defaults/patterns init.sh reads), `config/service.yaml`
(ownership metadata init.sh writes). Three content layers
(runtime/foundation/engine) are mapped in `docs/architecture/overview.md`.
Docs map: `docs/api|events|operations|architecture|security|testing|
development|adr` (+ `workflows/`, `modules.json`).
