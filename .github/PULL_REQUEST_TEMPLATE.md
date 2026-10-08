## What / why

-
-

## Checklist

- [ ] `sh scripts/verify.sh` passes (format, checkstyle, tests, JaCoCo 70%)
- [ ] ArchUnit boundaries respected (`AGENTS.md` §1); tests live with the code they test
- [ ] New behavior covered by tests (unit → slice → IT where infra is involved)
- [ ] Module flags documented (`docs/modules.json`, `README.md`, config metadata) if changed
- [ ] Flyway migration added and backward-compatible (if schema changed)
- [ ] `CHANGELOG.md` updated under `Unreleased` (user-visible changes)
- [ ] No secrets/PII in diff or logs (`secret-scan` clean)

## Security considerations

- [ ] AuthN/Z impact reviewed (or N/A)
- [ ] New dependencies justified; SBOM (`target/bom.json`) reviewed
