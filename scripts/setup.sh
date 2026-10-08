#!/bin/sh
# setup: one-time developer setup. No network required.
# Usage: sh scripts/setup.sh
set -eu
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

command -v java >/dev/null || { echo "ERROR: java not found (need JDK 21+)"; exit 1; }
JAVA_MAJOR="$(java -version 2>&1 | head -n 1 | sed 's/.*version "\(.*\)".*/\1/' | cut -d. -f1)"
if [ "$JAVA_MAJOR" -lt 21 ]; then echo "ERROR: JDK 21+ required, found $JAVA_MAJOR"; exit 1; fi

if git rev-parse --git-dir >/dev/null 2>&1; then
  git config core.hooksPath .githooks
  git config commit.template .gitmessage
  echo "hooks: core.hooksPath -> .githooks, commit.template -> .gitmessage"
else
  echo "hooks: not a git repo, skipping hook install (run inside a checkout)"
fi
echo "java: $(java -version 2>&1 | head -n 1)"
echo "next: sh scripts/verify.sh (full gates) or docker compose up -d for local infra"
