#!/bin/sh
# ci: portable CI entrypoint (any platform with sh + JDK 21 + Docker for ITs).
# Mirrors .github/workflows/ci.yml minus the GitHub-specific upload/docker steps.
set -eu
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
sh "$ROOT/scripts/verify.sh"
sh "$ROOT/scripts/security.sh"
echo "ci: done"
