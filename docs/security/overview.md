# Security overview

- Modes: `security.enabled=false` (default) → permit-all chain for local dev
  and gateway-terminated auth ONLY. `=true` → OAuth2 JWT resource server
  (`JWT_ISSUER_URI` required) + `@PreAuthorize` method security.
- Never commit secrets: env/secret manager only (`.env.example` documents
  names). `secret-scan.sh` (pre-commit) + Gitleaks (CI).
- Hardening the image supports (platform enforces in its manifests):
  non-root UID 1000, read-only FS compatible, dropped capabilities, seccomp
  compatible, CPU/mem limits respected. CSRF disabled (stateless API); CORS
  same-origin by default — configure per service when browsers call directly.
- Supply chain: CycloneDX SBOM per build, Trivy HIGH/CRITICAL gate in CI,
  Dependabot weekly. Policy: `SECURITY.md`.
