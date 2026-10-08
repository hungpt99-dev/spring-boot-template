#!/bin/sh
# security: SBOM + best-available scanners. Never fails for missing optional tools.
set -eu
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
sh "$ROOT/scripts/mvn.sh" -B -q package -DskipTests org.cyclonedx:cyclonedx-maven-plugin:makeAggregateBom
echo "sbom: $ROOT/target/bom.json"
sh "$ROOT/scripts/secret-scan.sh" --tracked
if command -v trivy >/dev/null; then
  trivy fs --severity HIGH,CRITICAL --exit-code 1 "$ROOT" || exit 1
else
  echo "security: trivy not installed, skipping fs scan (CI enforces it)"
fi
if command -v gitleaks >/dev/null; then
  (cd "$ROOT" && gitleaks detect --no-git -v) || exit 1
else
  echo "security: gitleaks not installed, skipping (CI enforces it)"
fi
echo "security: done"
