# Contributing

## Developer workflow (same commands locally and in CI)

Git Bash (Windows) or any POSIX shell. PowerShell equivalents call
`./mvnw` directly with the same Maven goals shown in each script.

| Command | What it runs | When |
|---|---|---|
| `sh scripts/setup.sh` | Checks JDK 21+, points git at `.githooks/` | Once per clone |
| `sh scripts/format.sh` | `spring-javaformat:apply` | Before commit |
| `sh scripts/check.sh` | Format validate + Checkstyle (fast, no tests) | pre-commit hook |
| `sh scripts/test.sh` | Unit + slice + ArchUnit (`mvn test`) | pre-push hook |
| `sh scripts/integration-test.sh` | Testcontainers `*IT` (needs Docker) | Before PR touching infra code |
| `sh scripts/verify.sh` | Full gate: format, checkstyle, all tests, JaCoCo 70%, SBOM | Before PR |
| `sh scripts/build.sh` | Fast jar (`package -DskipTests`) | Local runs |
| `sh scripts/security.sh` | SBOM + secret scan + Trivy/gitleaks if installed | Before PR touching deps |
| `sh scripts/ci.sh` | Portable CI entrypoint (`verify` + `security`) | Any CI platform |
| `sh scripts/validate-template.sh` | Template self-check (metadata, hooks, build, k8s, OpenAPI, secrets) | After template changes |

Quality layers: hooks (fast) → local verify → PR review → CI (full gates +
Trivy + gitleaks + Docker build). Expensive checks (ITs, security scans) live
in CI; hooks stay under ~30s.

## Branches, commits, PRs

- Branches: `feat/<scope>`, `fix/<scope>`, `chore/<scope>` from `main`.
- Commits: Conventional Commits (enforced by `commit-msg` hook, template in
  `.gitmessage`). One logical change per commit.
- PRs: fill `.github/PULL_REQUEST_TEMPLATE.md`; keep diffs reviewable (<400
  lines preferred); `mvn verify` must pass; update `CHANGELOG.md` under
  `Unreleased` for user-visible changes.

## Versioning and releases

- SemVer (`MAJOR.MINOR.PATCH`); `main` stays `-SNAPSHOT`.
- Release: update version (remove `-SNAPSHOT`), `CHANGELOG.md` entry, tag
  `vX.Y.Z`, push tag; CI builds the image from the tag. Next commit re-adds
  `-SNAPSHOT`. No `maven-release-plugin` by design (tags + changelog are the
  source of truth).
- Boot/Java upgrades: follow `docs/UPGRADE.md` (supported lines only).

## Agent notes

AI agents follow `AGENTS.md` + `docs/workflows/`. Architecture is validated by
ArchUnit — do not work around it; change the design instead. Never hand-edit
template identifiers; use `sh scripts/init.sh` (see
`docs/workflows/create-service.md`).
