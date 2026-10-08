# Dead-letter queues

Poison messages (permanently failing after retries) go to
`<topic>.dlq` with headers: original topic/partition/offset, `event-id`,
failure reason, attempt count. Alert on DLQ growth. Replay procedure:
fix the bug or the message, re-publish to the source topic (preserving key
for ordering), monitor the consumer lag back to zero. Never silently drop
without a recorded decision.
