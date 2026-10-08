#!/bin/sh
# secret-scan: fail if staged changes contain likely secrets.
# Zero dependencies (pure git + grep). Usage: sh scripts/secret-scan.sh [--staged|--tracked]
set -eu
MODE="${1:---staged}"
case "$MODE" in
  --staged|--tracked) ;;
  *) echo "usage: secret-scan.sh [--staged|--tracked]"; exit 2 ;;
esac
if ! git rev-parse --git-dir >/dev/null 2>&1; then
  echo "secret-scan: not a git repo, skipping"
  exit 0
fi
PATTERN='-----BEGIN [A-Z ]*PRIVATE KEY-----|AKIA[0-9A-Z]{16}|aws_secret_access_key|ghp_[A-Za-z0-9]{36}|github_pat_[A-Za-z0-9_]+|xox[bpas]-[A-Za-z0-9-]+|AIza[0-9A-Za-z_-]{35}|password\s*=\s*["'\''][^"'\'']+["'\'']|passwd\s*=\s*["'\''][^"'\'']+["'\'']'
if [ "$MODE" = "--staged" ]; then
  HITS="$(git diff --cached --unified=0 -- . | grep -nE "^\+[^+]" | grep -E -e "$PATTERN" || true)"
else
  HITS="$(git grep -n -I -E -e "$PATTERN" -- . || true)"
fi
ALLOWLIST=".secret-allowlist"
if [ -f "$ALLOWLIST" ] && [ -n "$HITS" ]; then
  HITS="$(echo "$HITS" | grep -v -F -f "$ALLOWLIST" || true)"
fi
if [ -n "$HITS" ]; then
  echo "ERROR: possible secrets detected (add false positives to .secret-allowlist):"
  echo "$HITS"
  exit 1
fi
echo "secret-scan: clean"
