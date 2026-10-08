#!/bin/sh
# Resolve repo root (works from any cwd inside the repo).
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
MVN="$ROOT/mvnw"
if [ ! -x "$MVN" ]; then MVN="mvn"; fi
exec "$MVN" "$@"
