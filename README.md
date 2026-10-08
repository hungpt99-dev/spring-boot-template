# Microservice Template

Company-standard, AI-agent-friendly Spring Boot template. Lightweight by
default: boots with **zero infrastructure** (in-memory repo, no-op events,
permit-all security). Enable capabilities one flag at a time.

Agents start at `AGENTS.md` (boundaries, patterns, gates, workflows).
Machine-readable module registry: `docs/modules.json`.

## Stack (pinned for stability + support lifecycle, Oct 2026)

| Choice | Version | Why |
|---|---|---|
| Java | 21 LTS (Corretto/Temurin) | LTS to 2030+, virtual threads, Boot 4 supports 17–26 |
| Spring Boot | 4.1.1 | Latest supported line (security fixes to Jul 2027); 3.5.x is EOL since Jun 2026 |
| Spring Framework / Jakarta | 7.0.x / EE 11 | Managed by Boot BOM |
| Build | Maven 3.9+, `spring-boot-starter-parent` | Company default; version management via BOM |
| DB / migrations | PostgreSQL 17 + Flyway | SQL-first, `ddl-auto: validate`, expand-then-contract |
| Cache | Redis 7 (Lettuce) | Non-blocking client; JSON value codec |
| Messaging | Kafka 3.9 (spring-kafka, KRaft) | Idempotent producer defaults (`acks=all`) |
| API docs | springdoc-openapi 3.1.1 (Boot 4 line) | No extra config; follows module flag |
| Resilience | Resilience4j 2.4.0 (`spring-boot4` starter) | Retry + circuit breaker as decorator, not in logic |
| Events | Transactional outbox + shared envelope codec | Atomic publish via `OutboxStore`/`OutboxRelay`; see `docs/EVENTS.md` |
| Quality | spring-javaformat 0.0.48, ArchUnit 1.4.1, JaCoCo gate 70% | Format check + arch tests run in `mvn verify` |
| Static analysis | Checkstyle 14 (hygiene only) | `sh scripts/check.sh`; formatting owned by the formatter |
| Supply chain | CycloneDX SBOM + Trivy + Gitleaks | `sh scripts/security.sh`; gates in CI |
| Observability | Micrometer + Prometheus + OTel tracing + ECS logs | Native Boot structured logging, no custom framework |
| Tests | JUnit 5, Mockito, AssertJ, Testcontainers, ArchUnit 1.4.1, JaCoCo gate 70% | Test pyramid enforced |

Tomcat 11, Hibernate 6.6 (via BOM), HikariCP, Jackson. No Lombok (records
instead), no MapStruct (explicit mappers), no Spring Cloud (unneeded weight).

## Layout (Hexagonal-lite)

```
com.company.template
├── TemplateApplication.java
├── domain/model/          # pure Java records, zero framework deps (ArchUnit-enforced)
├── app/port/{in,out}/     # use-case + SPI interfaces
├── app/service/           # use cases (DefaultProductService)
├── adapter/in/web/        # controllers, DTOs, mappers
├── adapter/out/persistence|messaging/  # JPA / in-memory, Kafka / no-op
└── common/                # config, error, web, security, cache, resilience, idempotency, modules
```

Rules: controllers stay thin; domain never imports Spring/Jakarta; `app`
never imports `adapter`; inbound adapters never import outbound ones.

## Modules (one flag each)

| Flag `app.modules.*.enabled` | Default | When true |
|---|---|---|
| `persistence` | false | JPA + Flyway + Postgres (needs `DB_URL/DB_USER/DB_PASSWORD`) |
| `redis` | false | Redis cache backend |
| `kafka` | false | Kafka producer (needs broker) |
| `security` | false | OAuth2 JWT resource server (needs `JWT_ISSUER_URI`) |
| `resilience` | false | Retry/circuit-breaker decorator |
| `outbox` | false | Transactional outbox (needs `persistence` + `kafka`; fail-fast otherwise) |
| `idempotency` | true | `Idempotency-Key` replay (in-memory; clustered deploys need shared store) |
| `docs` | true | OpenAPI + Swagger UI |

Disabled persistence/redis/kafka are **excluded from auto-configuration** by
`ModularEnvironmentPostProcessor` before Boot starts, so no beans and no
connection attempts. Env form: `APP_MODULES_KAFKA_ENABLED=true`.

## Quick start

```bash
# 1. One-time setup (JDK 21 check + git hooks) and fast checks
sh scripts/setup.sh
sh scripts/check.sh    # format + checkstyle, also runs on pre-commit
sh scripts/test.sh     # unit + slice + ArchUnit, also runs on pre-push

# 2. Zero-infra run + full gates
sh scripts/verify.sh
sh scripts/build.sh && java -jar target/*.jar
curl localhost:8080/api/v1/products

# 3. Full stack locally
docker compose up -d
mvn spring-boot:run -Dspring-boot.run.profiles=local
# Swagger: http://localhost:8080/swagger-ui.html
# Health:  http://localhost:8080/actuator/health

# 4. Docker (packaging is an app-repo responsibility)
docker build -t template:dev .
# Deploy: manifests live in the deployment repo — hand it the image plus
# docs/operations/kubernetes.md (probe/shutdown/env/scaling contract).
```

## New service from this template

```bash
sh scripts/init.sh --service-name payment-service --group-id com.company \
  --package-name com.company.payment --domain billing --owner payments-team
# or interactive: sh scripts/init.sh
```

init copies to `../payment-service`, renames package/class/pom/CI/docs
per `scripts/template-vars.sh` (defaults/patterns from
`.template/variables.yaml`), writes `config/service.yaml` ownership, drops
template tooling (`.template/` + engine scripts), checks for leftover
identifiers (`scripts/check-template.sh`), and runs `verify` in the new tree.
Details: `docs/workflows/create-service.md`. Then follow
`docs/workflows/add-endpoint.md` to replace the sample domain.

Repo layers: **runtime** (code/config/image the service runs) · **foundation**
(scripts/hooks/CI/docs) · **engine** (`.template/` + init/validate tooling,
removed on init). K8s manifests are intentionally absent (deployment repo
owns them — see `docs/operations/kubernetes.md`). Full map: `docs/architecture/overview.md`.

Validate the template itself: `sh scripts/validate-template.sh` (metadata,
hooks, build, k8s, OpenAPI, secrets).

## Conventions (short)

- **Config**: `application.yml` defaults + env overrides; secrets only from env/secret manager.
- **Errors**: RFC 9457 `ProblemDetail` with `code` (`TEMPLATE_*`) + `correlationId`; see `GlobalExceptionHandler`.
- **Logging**: ECS structured console; one line per request; `X-Correlation-Id` honored/generated; trace ids automatic via OTel bridge.
- **Testing**: unit (no context) → web slice → ArchUnit → Testcontainers IT (`@ServiceConnection`, `disabledWithoutDocker`); inject `Clock`, never `Instant.now()`.
- **Security**: JWT resource server + `@PreAuthorize`; permit-all chain exists for dev/gateway-terminated auth only.
- **Migrations**: `V<n>__<name>.sql`, backward-compatible, never edit released files.
- **Idempotency**: clients send `Idempotency-Key` on POST/PUT/PATCH/DELETE.
- **Shutdown**: graceful + 30s phase timeout + K8s `preStop` sleep; virtual threads on.

Details: `docs/OPERATIONS.md`, `docs/UPGRADE.md`, `docs/EVENTS.md`.
Docs by area: `docs/api|events|operations|architecture|security|testing|
development|adr`. Agent instructions: `AGENTS.md` + `docs/workflows/` +
`docs/modules.json`.
