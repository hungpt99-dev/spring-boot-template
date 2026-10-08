# Upgrade strategy

- **Cadence**: follow Spring Boot supported lines; upgrade within 1 quarter of a
  new minor (4.x) and immediately for CVEs (Dependabot + Trivy in CI).
- **Line policy**: stay on the newest supported minor with ≥6 months support
  remaining. 3.5.x went EOL Jun 2026 — this template targets 4.1.x (to Jul 2027).
- **Java**: stay on the current LTS (21 now; evaluate 25 after ecosystem
  bake-in, incl. native-image and vendor support).
- **Process**:
  1. Bump `spring-boot-starter-parent` on a branch; run `mvn verify`.
  2. Read Boot + Framework release notes for baseline changes (Jakarta, Hibernate, Flyway).
  3. Re-run full suite + container build + Trivy; deploy to staging, watch
     error budget (5xx, latency p99, Flyway migrate time).
  4. Update this file's version table and `info.app.version`.
- **Pinned extras** (not in Boot BOM): `springdoc`, `resilience4j`, `archunit`
  — check their Boot-compat matrix before bumping Boot major versions.
- **Never**: EOL Boot lines in production, `ddl-auto: update`, editing released
  Flyway migrations, or pinning versions the BOM already manages.

## Boot 4 deltas already handled by this template (do not regress)

- Auto-config packages moved out of `org.springframework.boot.autoconfigure.*`:
  `boot.jdbc / boot.hibernate / boot.flyway / boot.data.redis / boot.kafka`.
  `ModularEnvironmentPostProcessor` excludes the new FQNs; Kafka/Flyway need
  their `spring-boot-kafka` / `spring-boot-flyway` carrier modules.
- `spring-boot-starter-aop` removed → `spring-boot-starter-aspectj`.
- `@WebMvcTest` moved to `spring-boot-webmvc-test`
  (`org.springframework.boot.webmvc.test.autoconfigure`); add the
  `spring-boot-starter-webmvc-test` test dependency.
- `@MockitoBean` moved to Spring Framework
  (`org.springframework.test.context.bean.override.mockito`).
- `EnvironmentPostProcessor` moved to `org.springframework.boot` root package;
  register via `META-INF/spring.factories` under the new key.
- Jackson 3 (`tools.jackson`): `WRITE_DATES_AS_TIMESTAMPS` is gone (ISO-8601
  default); `GenericJackson2JsonRedisSerializer` → `RedisSerializer.json()`.
- Testcontainers 2.x: artifacts renamed with `testcontainers-` prefix
  (`testcontainers-junit-jupiter`, `testcontainers-postgresql`);
  `PostgreSQLContainer` moved to `org.testcontainers.postgresql` and is non-generic.
- JaCoCo is no longer version-managed by the Boot parent — pin it explicitly
  (template: 0.8.13).
