#!/bin/sh
# build: produce the runnable jar fast (skips tests; use test.sh/verify.sh for gates).
set -eu
exec sh "$(dirname "$0")/mvn.sh" -B -q package -DskipTests
