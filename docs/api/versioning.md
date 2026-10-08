# API versioning

- URL major versions only: `/api/v1/...`. Minor, backward-compatible changes
  (new optional fields, new endpoints) do NOT bump the version.
- Breaking changes (remove/rename field, change semantics) → new major
  (`/api/v2/...`); run both for at least one release with a sunset date.
- Deprecations: mark in OpenAPI (`@Deprecated` + `deprecated: true`) and in
  `CHANGELOG.md`; never remove without a migration note.
- Event type versions (`product.created.v1`) are independent of URL versions
  (see `docs/events/versioning.md`).
