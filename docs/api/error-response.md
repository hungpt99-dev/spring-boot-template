# Error responses (RFC 9457)

Every error is a `ProblemDetail` from `GlobalExceptionHandler`:

```json
{
  "title": "Not found",
  "status": 404,
  "detail": "Product not found: abc",
  "instance": "/api/v1/products/abc",
  "code": "TEMPLATE_NOT_FOUND",
  "correlationId": "7f3a..."
}
```

Codes (`code` is contract, `detail` is human text and may change):

| HTTP | `code` | Trigger |
|---|---|---|
| 400 | `TEMPLATE_VALIDATION_FAILED` | Bean validation on DTOs |
| 400 | `TEMPLATE_BAD_REQUEST` | `IllegalArgumentException`, malformed body |
| 404 | `TEMPLATE_NOT_FOUND` | `NoSuchElementException` |
| 500 | `TEMPLATE_INTERNAL_ERROR` | Anything else (logged server-side, detail sanitized) |

Rules: new category → new `TEMPLATE_*` code + test; never leak stack traces,
SQL, or secrets in `detail`; always include `correlationId` for log correlation.
