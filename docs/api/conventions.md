# API conventions

- Resource URLs: plural nouns, kebab-case, versioned root: `/api/v1/products`.
- Verbs in methods, never in URLs. Filtering/sorting/pagination via query params.
- DTOs are records with Jakarta validation; domain records stay framework-free.
- Controllers are thin: HTTP + `@Valid` + manual mapper. No business logic.
- Time format: ISO-8601 UTC strings (Jackson 3 default). Money: integer minor
  units (`priceCents`), never floats.
- OpenAPI is generated from code (springdoc, `/v3/api-docs`, UI at
  `/swagger-ui.html`); metadata via `OpenApiConfig`. No hand-written spec —
  `validate-template.sh` asserts `/v3/api-docs` is valid JSON.
  Copy `openapi.yaml` (skeleton: paths, ProblemDetail, pagination,
  Idempotency-Key) when designing a real resource.
- See: `error-response.md`, `pagination.md`, `versioning.md`, `idempotency.md`, `openapi.yaml`.
