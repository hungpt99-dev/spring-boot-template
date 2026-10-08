# Workflow: create a service from this template

Use `sh scripts/init.sh` — never hand-rename identifiers. Requires GNU
userland (`tar`, `sed -i`, `awk`): Linux and Git Bash are fine; on macOS run
inside Docker (`docker run --rm -v "$PWD:/w" -w /w eclipse-temurin:21-jdk
sh scripts/init.sh ...`) or install gnu-sed/tar. Flags:
`--service-name` (required, `^[a-z0-9-]+$`), `--group-id`, `--package-name`
(must start with group-id), `--description`, `--main-class`
(default `<PascalService>Application`), `--title`, `--target-dir`
(default `../<service-name>`), `--skip-verify`, `--force`.

1. Run non-interactively (CI/automation) or bare for prompts:
   `sh scripts/init.sh --service-name payment-service --group-id com.company --package-name com.company.payment`
2. init copies the tree (minus `target/`, `logs/`, `.git`), moves the Java
   package dirs, rewrites `package`/`import` prefixes, renames the main class,
   updates `pom.xml` / `application.yml` / CI / docs per
   `scripts/template-vars.sh`, and deletes template-only files (`init.sh`,
   `template-vars.sh`, `check-template.sh`).
3. init runs `sh scripts/check-template.sh <dir>` (no leftover identifiers)
   then `sh scripts/verify.sh` in the generated tree unless `--skip-verify`.
4. Result is independent: own git repo, own CI, no reference to the template.
   Continue with `docs/workflows/add-endpoint.md` to replace the sample domain.
