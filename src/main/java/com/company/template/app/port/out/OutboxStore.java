package com.company.template.app.port.out;

import java.time.Instant;
import java.util.UUID;

/**
 * Outbound port: transactional outbox SPI. The JPA adapter implements this;
 * {@link OutboxStore} rows are written in the SAME transaction as the aggregate change,
 * then relayed to Kafka asynchronously. Consumers must be idempotent (at-least-once
 * delivery).
 */
public interface OutboxStore {

	void append(OutboxMessage message);

	java.util.List<OutboxMessage> claimBatch(int limit, int maxAttempts);

	void markPublished(UUID id);

	void markFailed(UUID id, String error);

	/** Immutable outbox row. Payload is a JSON document produced by the event codec. */
	record OutboxMessage(UUID id, String aggregateType, String aggregateId, String type, String topic, String payload,
			Instant createdAt, Instant publishedAt, int attempts) {
	}

}
