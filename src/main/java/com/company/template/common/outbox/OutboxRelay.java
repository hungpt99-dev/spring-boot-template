package com.company.template.common.outbox;

import com.company.template.app.port.out.OutboxStore;
import com.company.template.app.port.out.OutboxStore.OutboxMessage;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Outbox relay: polls claimed rows and publishes them to Kafka (at-least-once).
 * {@code FOR UPDATE SKIP LOCKED} claiming makes N replicas safe. Failures bump
 * {@code attempts}; rows past {@code max-attempts} need operator attention (see
 * {@code docs/OPERATIONS.md}). Consumers MUST be idempotent on {@code event-id} header.
 */
@Component
@ConditionalOnProperty(prefix = "app.modules", name = "outbox.enabled", havingValue = "true")
@ConditionalOnProperty(prefix = "app.modules", name = "kafka.enabled", havingValue = "true")
public class OutboxRelay {

	private static final Logger log = LoggerFactory.getLogger(OutboxRelay.class);

	private final OutboxStore store;

	private final KafkaTemplate<String, String> kafka;

	private final int batchSize;

	private final int maxAttempts;

	private final Duration sendTimeout;

	public OutboxRelay(OutboxStore store, KafkaTemplate<String, String> kafka,
			@Value("${app.outbox.batch-size:50}") int batchSize,
			@Value("${app.outbox.max-attempts:10}") int maxAttempts,
			@Value("${app.outbox.send-timeout:PT10S}") Duration sendTimeout) {
		this.store = store;
		this.kafka = kafka;
		this.batchSize = batchSize;
		this.maxAttempts = maxAttempts;
		this.sendTimeout = sendTimeout;
	}

	@Scheduled(fixedDelayString = "${app.outbox.poll-interval:PT5S}")
	public void relay() {
		List<OutboxMessage> batch = store.claimBatch(batchSize, maxAttempts);
		for (OutboxMessage message : batch) {
			publish(message);
		}
	}

	private void publish(OutboxMessage message) {
		try {
			ProducerRecord<String, String> record = new ProducerRecord<>(message.topic(), message.aggregateId(),
					message.payload());
			record.headers().add("event-id", message.id().toString().getBytes());
			record.headers().add("event-type", message.type().getBytes());
			kafka.send(record).get(sendTimeout.toMillis(), TimeUnit.MILLISECONDS);
			store.markPublished(message.id());
		}
		catch (Exception ex) {
			log.warn("Outbox publish failed for event {} (attempt {})", message.id(), message.attempts(), ex);
			store.markFailed(message.id(), ex.getMessage());
		}
	}

}
