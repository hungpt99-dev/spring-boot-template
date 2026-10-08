#!/bin/sh
# format: normalize all sources with spring-javaformat.
set -eu
exec sh "$(dirname "$0")/mvn.sh" -B -q spring-javaformat:apply
