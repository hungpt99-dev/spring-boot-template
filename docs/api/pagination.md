# Pagination

List endpoints accept `page` (0-based, default 0) and `size` (default 20,
max 200). Invalid values → 400 `TEMPLATE_BAD_REQUEST`.

Ordering is server-defined and stable (e.g. by name); do not rely on
insertion order. Cursor pagination is preferred for large or
frequently-mutated collections — add it per service when offset cost matters.
Response shape stays a plain JSON array for small collections; use a
`{content, page, size, totalElements}` envelope only when clients need totals.
