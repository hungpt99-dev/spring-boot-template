# Troubleshooting

| Symptom | Likely cause | Fix |
|---|---|---|
| Boot fails: `Failed to determine a suitable driver class` | persistence on without `DB_URL` | Set datasource env (see `.env.example`) |
| Boot fails: `outbox requires persistence+kafka` | Flag combo invalid | Enable both or turn outbox off |
| Context fails: `Could not bind JacksonProperties` | Removed/renamed Jackson key | Jackson 3 dropped date-timestamp features; check Boot docs |
| 401 everywhere locally | `security.enabled` without issuer | Set `JWT_ISSUER_URI` or disable locally |
| Kafka send hangs | No broker / wrong `KAFKA_BOOTSTRAP_SERVERS` | `docker compose up -d kafka`; check advertised listener |
| ITs all skip | No Docker daemon | Start Docker Desktop; `disabledWithoutDocker` skips silently |
| JaCoCo gate fails | New code untested | Add unit tests; live-infra wiring excluded (see pom) |
| Checkstyle fails on `log` | Rule intentionally dropped | Do not re-add `ConstantName` for loggers |
