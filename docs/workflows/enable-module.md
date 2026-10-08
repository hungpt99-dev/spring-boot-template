# Workflow: enable (or add) a module

Enabling (no code): set `app.modules.<name>.enabled=true` (or env
`APP_MODULES_<NAME>_ENABLED=true`), add the infra (see `compose.yaml` for
local), set secrets via env, run `mvn verify` + boot the app.

Adding a NEW optional capability:
1. Flag `app.modules.<name>.enabled` (default: the lightweight choice) in
   `ModuleProperties` (nested `Module` object), `application.yml`,
   `additional-spring-configuration-metadata.json`, `docs/modules.json`.
2. Gate every bean with repeatable `@ConditionalOnProperty` (AND semantics).
3. If it brings Boot auto-config: add Boot 4 FQNs to
   `ModularEnvironmentPostProcessor` + extend its test (verify disabled boot
   creates no beans / needs no infra via `TemplateApplicationTests`-style test).
4. Zero-infra fallback (in-memory / no-op / `@ConditionalOnMissingBean` in a
   `@Configuration` selector) OR fail-fast `ApplicationRunner` validator.
5. Flyway migration if it needs tables (V<n>__, backward-compatible only).
6. Tests per pyramid + `mvn spring-javaformat:apply` + `mvn verify`.
