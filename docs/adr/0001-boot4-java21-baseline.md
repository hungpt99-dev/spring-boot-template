# 0001. Spring Boot 4.1 + Java 21 baseline

Date: 2026-10-08 | Status: accepted

## Context

Boot 3.5.x reached end-of-life (Jun 2026); 4.0 ends Dec 2026. Only 4.1.x
receives security fixes into 2027. Java 21 is the current LTS with virtual
threads; Java 25 is too new for the native-image and vendor ecosystem.

## Decision

Boot 4.1.x, Java 21, Maven. BOM-managed versions everywhere except
springdoc, Resilience4j, ArchUnit, JaCoCo, formatters (pinned, documented).

## Consequences

Boot 4 repackaging (auto-config modules, test starters, `tools.jackson`,
Testcontainers 2.x) is absorbed once in the template; services inherit it.
Upgrade policy in `docs/UPGRADE.md`.
