#!/bin/sh
# verify: the full gate (format, checkstyle, all tests incl. ITs, JaCoCo, SBOM).
# Needs Docker for ITs (they skip cleanly without it).
set -eu
exec sh "$(dirname "$0")/mvn.sh" -B verify
