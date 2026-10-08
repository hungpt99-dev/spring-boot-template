#!/bin/sh
# init.sh - turn this TEMPLATE into an independent service.
#
# Non-interactive:
#   sh scripts/init.sh --service-name payment-service --group-id com.company \
#     --package-name com.company.payment [--description "..."] \
#     [--main-class PaymentServiceApplication] [--title "Payment Service"] \
#     [--domain billing] [--owner payments-team] \
#     [--target-dir ../payment-service] [--skip-verify] [--force]
# Interactive: run with no flags and answer the prompts.
#
# Defaults and validation patterns come from .template/variables.yaml
# (see tvar() below). Per-file transformations are listed in
# scripts/template-vars.sh. Flow: collect -> copy -> transform -> clean ->
# validate (check-template) -> verify (unless --skip-verify).
set -eu

usage() {
  sed -n '2,/^set -eu/p' "$0" | sed 's/^# \{0,1\}//'
  exit "${1:-0}"
}

SERVICE_NAME=""; GROUP_ID=""; PACKAGE_NAME=""; DESCRIPTION=""
MAIN_CLASS=""; TITLE=""; DOMAIN=""; OWNER=""
TARGET_DIR=""; SKIP_VERIFY=0; FORCE=0
while [ $# -gt 0 ]; do
  case "$1" in
    --service-name) SERVICE_NAME="$2"; shift 2 ;;
    --group-id) GROUP_ID="$2"; shift 2 ;;
    --package-name) PACKAGE_NAME="$2"; shift 2 ;;
    --description) DESCRIPTION="$2"; shift 2 ;;
    --main-class) MAIN_CLASS="$2"; shift 2 ;;
    --title) TITLE="$2"; shift 2 ;;
    --domain) DOMAIN="$2"; shift 2 ;;
    --owner) OWNER="$2"; shift 2 ;;
    --target-dir) TARGET_DIR="$2"; shift 2 ;;
    --skip-verify) SKIP_VERIFY=1; shift ;;
    --force) FORCE=1; shift ;;
    -h|--help) usage 0 ;;
    *) echo "unknown flag: $1"; usage 1 ;;
  esac
done

TEMPLATE_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
# shellcheck disable=SC1091
. "$TEMPLATE_ROOT/scripts/template-vars.sh"
VARS_YAML="$TEMPLATE_ROOT/.template/variables.yaml"

# tvar <name> <field>: read default|pattern|description from variables.yaml.
tvar() {
  awk -v name="$1" -v field="$2" '
    $0 == "- name: " name { inblock = 1; next }
    inblock && /^  - name:/ { exit }
    inblock && $1 == field ":" { sub(/^[^:]*: */, ""); print; exit }
  ' "$VARS_YAML"
}

prompt() { # $1=var $2=text $3=default
  if [ -z "$(eval "echo \$$1")" ] && [ -t 0 ]; then
    printf '%s [%s]: ' "$2" "$3"
    IFS= read -r val || val=""
    if [ -z "$val" ]; then val="$3"; fi
    eval "$1=\"\$val\""
  fi
}

to_title() { # payment-service -> Payment Service
  echo "$1" | tr '-' ' ' | awk '{for(i=1;i<=NF;i++) $i=toupper(substr($i,1,1)) substr($i,2)}1'
}
to_main() { # payment-service -> PaymentServiceApplication
  echo "$1" | awk -F- '{for(i=1;i<=NF;i++) $i=toupper(substr($i,1,1)) substr($i,2)}1' OFS='' | tr -d ' '
}

prompt SERVICE_NAME "Service name" "$(tvar service-name default)"
prompt GROUP_ID "Group ID" "$(tvar group-id default)"
prompt PACKAGE_NAME "Java package" "$GROUP_ID.$(echo "$SERVICE_NAME" | tr '-' '.')"
prompt DOMAIN "Business domain" "$(tvar domain default)"
prompt OWNER "Owning team" "$(tvar owner default)"
[ -z "$TARGET_DIR" ] && TARGET_DIR="$TEMPLATE_ROOT/../$SERVICE_NAME"
[ -z "$MAIN_CLASS" ] && MAIN_CLASS="$(to_main "$SERVICE_NAME")Application"
[ -z "$TITLE" ] && TITLE="$(to_title "$SERVICE_NAME")"
[ -z "$DESCRIPTION" ] && DESCRIPTION="$TITLE microservice"

valid() { echo "$2" | grep -qE "$1"; }
valid "$(tvar service-name pattern)" "$SERVICE_NAME" || { echo "ERROR: pass --service-name or run in a terminal for prompts"; exit 1; }
valid "$(tvar group-id pattern)" "$GROUP_ID" || { echo "ERROR: bad --group-id"; exit 1; }
valid "$(tvar package-name pattern)" "$PACKAGE_NAME" || { echo "ERROR: bad --package-name"; exit 1; }
case "$PACKAGE_NAME" in "$GROUP_ID"*) ;; *) echo "ERROR: package must start with group-id"; exit 1 ;; esac
valid "$(tvar main-class pattern)" "$MAIN_CLASS" || { echo "ERROR: bad --main-class"; exit 1; }
valid "$(tvar domain pattern)" "$DOMAIN" || { echo "ERROR: bad --domain"; exit 1; }
valid "$(tvar owner pattern)" "$OWNER" || { echo "ERROR: bad --owner"; exit 1; }

if [ -e "$TARGET_DIR" ] && [ "$FORCE" -ne 1 ]; then
  echo "ERROR: $TARGET_DIR exists (use --force to overwrite)"
  exit 1
fi
rm -rf "$TARGET_DIR"
mkdir -p "$TARGET_DIR"
tar -cf - --exclude='./target' --exclude='./logs' --exclude='./.git' \
  --exclude='./.idea' --exclude='./.vscode' -C "$TEMPLATE_ROOT" . \
  | tar -xf - -C "$TARGET_DIR"
echo "copied -> $TARGET_DIR"

OLD_PKG_PATH="$(echo "$TEMPLATE_PACKAGE" | tr '.' '/')"
NEW_PKG_PATH="$(echo "$PACKAGE_NAME" | tr '.' '/')"
cd "$TARGET_DIR"

# 1. Move java trees to the new package (shared prefixes handled: only the
#    divergent suffix moves; emptied ancestors are pruned).
for root in src/main/java src/test/java; do
  if [ -d "$root/$OLD_PKG_PATH" ]; then
    mkdir -p "$root/$(dirname "$NEW_PKG_PATH")"
    rm -rf "$root/$NEW_PKG_PATH"
    mv "$root/$OLD_PKG_PATH" "$root/$NEW_PKG_PATH"
    rmdir -p "$root/$OLD_PKG_PATH" 2>/dev/null || true
  fi
done

# 2. Rewrite package/import prefixes inside moved java files.
find src -name '*.java' | while IFS= read -r f; do
  sed -i "s/^package $TEMPLATE_PACKAGE/package $PACKAGE_NAME/" "$f"
  sed -i "s/^import $TEMPLATE_PACKAGE/import $PACKAGE_NAME/" "$f"
  sed -i "s/importPackages(\"$TEMPLATE_PACKAGE\")/importPackages(\"$PACKAGE_NAME\")/" "$f"
done

# 3. Rename main class (file-scoped).
MAIN_DIR="src/main/java/$NEW_PKG_PATH"
TEST_DIR="src/test/java/$NEW_PKG_PATH"
if [ -f "$MAIN_DIR/$TEMPLATE_MAIN_CLASS.java" ]; then
  sed -i "s/$TEMPLATE_MAIN_CLASS/$MAIN_CLASS/g" "$MAIN_DIR/$TEMPLATE_MAIN_CLASS.java"
  mv "$MAIN_DIR/$TEMPLATE_MAIN_CLASS.java" "$MAIN_DIR/$MAIN_CLASS.java"
fi
TEST_MAIN="$TEST_DIR/${TEMPLATE_MAIN_CLASS}Tests.java"
if [ -f "$TEST_MAIN" ]; then
  sed -i "s/$TEMPLATE_MAIN_CLASS/$MAIN_CLASS/g" "$TEST_MAIN"
  mv "$TEST_MAIN" "$TEST_DIR/${MAIN_CLASS}Tests.java"
fi

# 4. spring.factories FQN.
sed -i "s/$TEMPLATE_PACKAGE/$PACKAGE_NAME/" src/main/resources/META-INF/spring.factories

# 5. Maven coordinates (anchored project-level tags only).
sed -i "s|^  <groupId>$TEMPLATE_GROUP_ID</groupId>|  <groupId>$GROUP_ID</groupId>|" pom.xml
sed -i "s|^  <artifactId>$TEMPLATE_SERVICE_NAME</artifactId>|  <artifactId>$SERVICE_NAME</artifactId>|" pom.xml
sed -i "s|^  <name>$TEMPLATE_SERVICE_NAME</name>|  <name>$SERVICE_NAME</name>|" pom.xml
sed -i "s|^  <description>.*</description>|  <description>$DESCRIPTION</description>|" pom.xml
sed -i "s|com/company/template/|$(echo "$PACKAGE_NAME" | tr '.' '/')/|" pom.xml
sed -i "s/TemplateApplication/$MAIN_CLASS/" pom.xml
sed -i "/Company-standard microservice template. Rename groupId/d" pom.xml

# 6. App + CI names. (No k8s transform: manifests live in the deployment repo â€”
# the handoff is docs/operations/kubernetes.md, see ADR-0004.)
sed -i "s/^    name: $TEMPLATE_SERVICE_NAME/    name: $SERVICE_NAME/" src/main/resources/application.yml
sed -i "s/$TEMPLATE_SERVICE_NAME/$SERVICE_NAME/g" .github/workflows/ci.yml

# 7. Service ownership metadata (explicit keys).
sed -i "s/^  name: .*/  name: $SERVICE_NAME/" config/service.yaml
sed -i "s/^  description: .*/  description: $DESCRIPTION/" config/service.yaml
sed -i "s/^  domain: .*/  domain: $DOMAIN/" config/service.yaml
sed -i "s/^  owner: .*/  owner: $OWNER/" config/service.yaml
sed -i "s/@platform-team/@$OWNER/g" .github/CODEOWNERS

# 8. Docs (explicit files only).
sed -i "s/^# $TEMPLATE_TITLE/# $TITLE/" README.md
sed -i "s/$TEMPLATE_PACKAGE/$PACKAGE_NAME/g" README.md AGENTS.md
sed -i "s/$TEMPLATE_MAIN_CLASS\.java/$MAIN_CLASS.java/" README.md
sed -i "s/template:dev/$SERVICE_NAME:dev/" README.md
sed -i "s/${TEMPLATE_MAIN_CLASS}Tests/${MAIN_CLASS}Tests/g" docs/workflows/enable-module.md
# Generated service is not a template: drop the template rule (section 8, last section).
sed -i '/^## 8\. Template rule/,$d' AGENTS.md

# 9. Drop template-only tooling (generated service is independent).
rm -rf .template scripts/init.sh scripts/template-vars.sh scripts/check-template.sh \
  scripts/validate-template.sh

# 10. Leftover validation (template's own checker against the generated tree).
sh "$TEMPLATE_ROOT/scripts/check-template.sh" "$TARGET_DIR"

# 11. Verify the generated service builds green.
if [ "$SKIP_VERIFY" -eq 1 ]; then
  echo "init done (verify skipped). Next: cd $TARGET_DIR && sh scripts/verify.sh"
else
  echo "verifying generated service..."
  (cd "$TARGET_DIR" && sh scripts/verify.sh)
  echo "init done: $TARGET_DIR ($SERVICE_NAME, $PACKAGE_NAME, $MAIN_CLASS)"
fi
