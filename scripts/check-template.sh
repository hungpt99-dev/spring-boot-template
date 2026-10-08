#!/bin/sh
# check-template.sh ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â fail if template identifiers leak into a GENERATED service.
# Usage: sh scripts/check-template.sh <dir>
# Excludes: .git, target/, logs/, CHANGELOG.md (release history may name the template).
set -eu
DIR="${1:?usage: check-template.sh <dir>}"
LEFTOVERS="$(grep -r -I -n -e 'com\.company\.template' -e 'TemplateApplication' \
  -e 'microservice-template' --exclude-dir=.git --exclude-dir=target \
  --exclude-dir=logs --exclude=CHANGELOG.md "$DIR" || true)"
if [ -n "$LEFTOVERS" ]; then
  echo "ERROR: template identifiers left behind:"
  echo "$LEFTOVERS"
  exit 1
fi
echo "check-template: clean ($DIR)"
