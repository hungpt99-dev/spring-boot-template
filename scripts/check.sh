#!/bin/sh
# check: fast static gates (format + checkstyle). No tests. Used by pre-commit.
set -eu
exec sh "$(dirname "$0")/mvn.sh" -B spring-javaformat:validate checkstyle:check
