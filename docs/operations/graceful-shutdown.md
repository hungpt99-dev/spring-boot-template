# Graceful shutdown

- `server.shutdown: graceful` + `spring.lifecycle.timeout-per-shutdown-phase: 30s`:
  SIGTERM stops accepting, drains in-flight requests, then closes the context.
- K8s `preStop` sleep (10s) lets the endpoint deregister before SIGTERM.
- Virtual threads (`spring.threads.virtual.enabled`) keep drain fast under load.
- `@Scheduled` relays (outbox) finish the current poll; in-flight Kafka sends
  honor `app.outbox.send-timeout`. Consumers should commit offsets before exit.
