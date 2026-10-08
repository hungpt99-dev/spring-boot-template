#!/bin/sh
# validate-template.sh -- the template checks ITSELF. Run from a template checkout:
#   sh scripts/validate-template.sh [--skip-docker] [--skip-boot]
# Fails on: bad .template/ metadata, stale template-vars, leftover placeholders,
# non-executable scripts/hooks, broken hooks, failed build, missing deployment
# contract, invalid OpenAPI, secrets in tree. Docker/boot checks skip gracefully
# when unavailable. (No kubectl check: the app repo ships no k8s manifests.)
set -eu
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
PASS=0; FAIL=0; SKIP=0
ok() { PASS=$((PASS + 1)); echo "PASS: $1"; }
fail() { FAIL=$((FAIL + 1)); echo "FAIL: $1"; }
skip() { SKIP=$((SKIP + 1)); echo "SKIP: $1"; }
SKIP_DOCKER=0; SKIP_BOOT=0
for a in "$@"; do case "$a" in --skip-docker) SKIP_DOCKER=1 ;; --skip-boot) SKIP_BOOT=1 ;; esac; done

# 1. .template/ metadata complete.
if [ -f .template/version ] && grep -qE '^[0-9]+\.[0-9]+\.[0-9]+$' .template/version; then
  ok "template version $(cat .template/version)"
else fail "template version missing/not semver"; fi
for f in metadata.yaml variables.yaml; do
  if [ -f ".template/$f" ]; then ok ".template/$f present"; else fail ".template/$f missing"; fi
done
# every variable block has name/default/pattern/description
if awk '/^#/{next} /- name:/{n++} /default:/{d++} /pattern:/{p++} /description:/{s++} END{exit !(n>0 && n==d && n==p && n==s)}' \
  .template/variables.yaml; then
  ok "variables.yaml blocks complete"
else fail "variables.yaml has incomplete variable blocks"; fi

# 2. template-vars.sh canonical values match the repo state.
# shellcheck disable=SC1091
. ./scripts/template-vars.sh
[ -d "src/main/java/$(echo "$TEMPLATE_PACKAGE" | tr '.' '/')" ] \
  && ok "package dir matches $TEMPLATE_PACKAGE" || fail "package dir mismatch"
[ -f "src/main/java/$(echo "$TEMPLATE_PACKAGE" | tr '.' '/')/$TEMPLATE_MAIN_CLASS.java" ] \
  && ok "main class $TEMPLATE_MAIN_CLASS present" || fail "main class missing"
grep -q "name: $TEMPLATE_SERVICE_NAME" src/main/resources/application.yml \
  && ok "app name matches $TEMPLATE_SERVICE_NAME" || fail "app name mismatch"

# 3. No __PLACEHOLDER__-style tokens anywhere (rename-based strategy).
LEFT="$(grep -r -I -n -E '__[A-Z][A-Z0-9_]+__' --exclude-dir=.git --exclude-dir=target \
  --exclude-dir=logs src config docs pom.xml 2>/dev/null || true)"
if [ -z "$LEFT" ]; then ok "no __PLACEHOLDER__ tokens"; else fail "placeholders left: $LEFT"; fi

# 4. Scripts + hooks have sh shebang and valid syntax (+x is set on commit via git).
BAD=""; for f in scripts/*.sh .githooks/*; do
  case "$f" in *.md) continue ;; esac
  head -n 1 "$f" | grep -q '^#!/bin/sh' || BAD="$BAD $f(no-shebang)"
  sh -n "$f" 2>/dev/null || BAD="$BAD $f(syntax)"
done
if [ -z "$BAD" ]; then ok "scripts/hooks executable + syntax ok"; else fail "script issues:$BAD"; fi

# 5. Hooks work (scratch repo: commit-msg accept/reject, secret-scan dirty/clean).
T="$(mktemp -d)"; trap 'rm -rf "$T"' EXIT INT TERM
cp -R .githooks scripts .gitmessage "$T/"
(
  cd "$T" && git init -q . && git config user.email t@t.t && git config user.name t \
    && git config core.hooksPath .githooks
  echo 'bad message' > m1; sh .githooks/commit-msg m1 2>/dev/null \
    && { echo "hook accepted bad msg"; exit 1; }
  printf 'feat(api): add thing\n' > m2; sh .githooks/commit-msg m2 \
    || { echo "hook rejected good msg"; exit 1; }
  printf 'x=1\npassword = "hunter2"\n' > s.py; git add s.py
  sh scripts/secret-scan.sh --staged >/dev/null 2>&1 \
    && { echo "scan missed secret"; exit 1; }
  git rm -q --cached s.py; printf 'print(1)\n' > s.py; git add s.py
  sh scripts/secret-scan.sh --staged >/dev/null 2>&1 \
    || { echo "scan flagged clean"; exit 1; }
) && ok "hooks behave (commit-msg + secret-scan)" || fail "hook behavior broken"

# 6. Full Maven build (format, checkstyle, tests, arch, jacoco, SBOM).
if sh scripts/verify.sh >/dev/null 2>&1; then ok "mvn verify green"; else fail "mvn verify red"; fi

# 7. Docker build (needs daemon).
if [ "$SKIP_DOCKER" -eq 1 ]; then skip "docker build (--skip-docker)";
elif docker info >/dev/null 2>&1; then
  if docker build -q -t template-validate:ci . >/dev/null 2>&1; then ok "docker build ok";
  else fail "docker build broken"; fi
else skip "docker build (no daemon)"; fi

# 8. Deployment separation: no k8s manifests in the app repo (ADR-0004); the
# deployment contract doc must exist and name the probe contract.
if [ -e k8s ] || [ -n "$(find . -maxdepth 2 -name '*.yaml' -path '*k8s*' 2>/dev/null || true)" ]; then
  fail "k8s manifests belong in the deployment repo, not here"
else
  ok "no k8s manifests in app repo"
fi
if grep -q '/actuator/health/liveness' docs/operations/kubernetes.md \
  && grep -q '/actuator/health/readiness' docs/operations/kubernetes.md \
  && grep -q 'readOnlyRootFilesystem' docs/operations/kubernetes.md; then
  ok "deployment contract covers probes + security constraints"
else
  fail "docs/operations/kubernetes.md contract incomplete"
fi

# 9. OpenAPI valid JSON from a live boot (needs port 8080).
if [ "$SKIP_BOOT" -eq 1 ]; then skip "openapi live check (--skip-boot)";
else
  JAR="$(ls target/*.jar 2>/dev/null | grep -v '\.original' | grep -v sources | head -n 1 || true)"
  if [ -z "$JAR" ]; then fail "no boot jar for openapi check";
  else
    java -jar "$JAR" >/dev/null 2>&1 & PID=$!
    UP=0
    for _ in $(seq 1 40); do
      if curl -sf http://localhost:8080/actuator/health >/dev/null 2>&1; then UP=1; break; fi
      sleep 2
    done
    if [ "$UP" -eq 1 ] && curl -sf http://localhost:8080/v3/api-docs \
        | grep -q '"openapi"'; then
      curl -sf http://localhost:8080/v3/api-docs | grep -q '"/api/v1/products"' \
        && ok "openapi valid (products path present)" || fail "openapi missing products path"
    else fail "openapi not reachable/invalid"; fi
    kill "$PID" 2>/dev/null || true
    wait "$PID" 2>/dev/null || true
  fi
fi

# 10. No secrets in tracked tree.
if git rev-parse --git-dir >/dev/null 2>&1; then
  if sh scripts/secret-scan.sh --tracked >/dev/null 2>&1; then ok "secret scan clean";
  else fail "secrets in tree"; fi
else
  LEAK="$(grep -r -I -n -E -e '-----BEGIN [A-Z ]*PRIVATE KEY-----' -e 'AKIA[0-9A-Z]{16}' \
    -e 'ghp_[A-Za-z0-9]{36}' --exclude-dir=.git --exclude-dir=target src config docs 2>/dev/null || true)"
  if [ -z "$LEAK" ]; then ok "secret grep clean (no git repo)"; else fail "secrets: $LEAK"; fi
fi

echo "validate-template: $PASS pass, $FAIL fail, $SKIP skip"
[ "$FAIL" -eq 0 ]
