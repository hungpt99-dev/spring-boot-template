# Testing strategy

Pyramid, each layer mapped to a command:

1. Pure unit (`sh scripts/test.sh`, no context): use cases with Mockito +
   fixed `Clock`; mappers, codec, validators, relay logic.
2. Web slice (`@WebMvcTest` from `...boot.webmvc.test.autoconfigure...`,
   Framework `@MockitoBean`): HTTP + validation + error mapping. Disable
   idempotency unless under test.
3. Architecture (`ArchitectureTest`): boundaries in §1 — never work around.
4. Integration (`*IT`, failsafe, `sh scripts/integration-test.sh`):
   Testcontainers + `@ServiceConnection`, `@Testcontainers(disabledWithoutDocker=true)`.
5. Contract (per service, when consumers exist): Spring Cloud Contract or
   Pact for REST; envelope-pinned tests + example payloads for events.

Gates: JaCoCo line 70% (`mvn verify`); live-infra wiring excluded from the
unit gate and covered by docker ITs in CI.
