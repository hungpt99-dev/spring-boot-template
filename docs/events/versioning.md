# Event versioning

Type format: `<aggregate>.<action>.v<n>` (e.g. `product.created.v1`).
Compatible changes (new optional payload fields) keep the version; consumers
must ignore unknown fields. Breaking changes (rename/remove/semantics) bump
`v<n>` and producers dual-publish old+new during migration. Never mutate a
published version in place. Record supported versions per consumer in the
service README or runbook.
