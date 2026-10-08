# Local development

```bash
sh scripts/setup.sh            # JDK check + hooks
docker compose up -d           # postgres, redis, kafka (KRaft)
sh scripts/build.sh && java -jar target/*.jar          # zero-infra defaults
# or full stack:
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

- Swagger: http://localhost:8080/swagger-ui.html
- Health: http://localhost:8080/actuator/health
- Debug: `./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"`
- `.env` is NOT read natively: source it (`set -a; . ./.env; set +a`) or use
  IDE run-config env. Never commit it.
