#!/bin/sh
# template-vars.sh - CENTRALIZED template variable manifest (sourced, not executed).
# Canonical template values + how each maps to init flags. init.sh applies ONLY
# the transformations listed here, file by file. No blind global search-replace.
# Flag defaults/patterns live in .template/variables.yaml (init.sh reads them).
#
# Placeholders are literal current values (rename-based strategy):
#   TEMPLATE_SERVICE_NAME  microservice-template  (--service-name)
#   TEMPLATE_GROUP_ID      com.company            (--group-id)
#   TEMPLATE_PACKAGE       com.company.template   (--package-name)
#   TEMPLATE_MAIN_CLASS    TemplateApplication    (--main-class, default derived)
#   TEMPLATE_TITLE         Microservice Template  (--title, default derived)
#
# Per-file transformations performed by init.sh:
#   pom.xml                                        project groupId/artifactId/name/description
#                                                + jacoco exclude path/class for the main class
#                                                + drop the template rename comment
#   docs/workflows/enable-module.md               sample test-class name
#   src/main|test/java/**.java (moved dirs)        ^package / ^import prefix rewrite
#   TemplateApplication.java -> <Main>.java        class rename (file-scoped)
#   ArchitectureTest.java                          importPackages("...") literal
#   META-INF/spring.factories                      post-processor FQN
#   src/main/resources/application.yml             spring.application.name
#   .github/workflows/ci.yml                       docker tag (file-scoped token)
#   (No k8s transform: manifests live in the deployment repo, ADR-0004.)
#   README.md                                      title, package tree, main-class file, docker tag
#   AGENTS.md                                      package tree; drop section 8 (template rule)
# Template-only files REMOVED from generated service:
#   .template/ scripts/init.sh scripts/template-vars.sh scripts/check-template.sh
#   scripts/validate-template.sh
# Service metadata written explicitly from flags (no placeholders):
#   config/service.yaml (name/description/domain/owner), .github/CODEOWNERS (@owner)
TEMPLATE_SERVICE_NAME="microservice-template"
TEMPLATE_GROUP_ID="com.company"
TEMPLATE_PACKAGE="com.company.template"
TEMPLATE_MAIN_CLASS="TemplateApplication"
TEMPLATE_TITLE="Microservice Template"
