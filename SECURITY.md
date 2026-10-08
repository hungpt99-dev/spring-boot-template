# Security policy

## Supported versions

Only the latest template release (Spring Boot supported line, see
`docs/UPGRADE.md`) receives fixes. Services generated from this template must
track supported Boot/Java lines independently.

## Reporting a vulnerability

Email the platform team at **security@company.local** with: affected version,
reproduction steps, and impact. Do not open public issues for suspected
vulnerabilities. Expect acknowledgment within 2 business days.

## Built-in controls

- CI fails on Trivy HIGH/CRITICAL filesystem findings and gitleaks findings.
- Every build emits a CycloneDX SBOM (`target/bom.json`, uploaded in CI).
- `secret-scan.sh` runs on staged changes (pre-commit) and full tree
  (`security.sh`); never commit secrets — use env/secret manager (`.env.example`).
- JWT resource-server + method security when `app.modules.security.enabled`;
  permit-all chain is dev/gateway-only (see `common/security/`).
