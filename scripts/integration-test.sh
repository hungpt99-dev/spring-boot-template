#!/bin/sh
# integration-test: Testcontainers *ITs only (failsafe). Needs Docker.
set -eu
exec sh "$(dirname "$0")/mvn.sh" -B test-compile failsafe:integration-test failsafe:verify
