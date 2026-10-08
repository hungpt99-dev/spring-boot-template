#!/bin/sh
# test: unit + slice + architecture tests (surefire). No Docker needed.
set -eu
exec sh "$(dirname "$0")/mvn.sh" -B test
