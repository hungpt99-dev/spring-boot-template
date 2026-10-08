package com.company.template.adapter.in.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Sample event consumer. Active only when {@code app.modules.kafka.enabled=true} AND
 * {@code app.kafka.consumer.enabled=true} (default false).
 *
 * <p>
 * Delivery is at-least-once: keep handlers idempotent — dedupe on {@code event-id} (see
 * {@code docs/events/idempotency.md}) before side effects. Key (aggregate id) preserves
 * per-aggregate ordering.
 */
@Component
@ConditionalOnProperty(prefix = "app.modules", name = "kafka.enabled", havingValue = "true")
@ConditionalOnProperty(prefix = "app.kafka.consumer", name = "enabled", havingValue = "true")
public class ProductEventConsumer {

	private static final Logger log = LoggerFactory.getLogger(ProductEventConsumer.class);

	@KafkaListener(topics = "${app.messaging.product-created-topic:products.events.v1}",
			groupId = "${spring.application.name}-consumer")
	public void onProductCreated(@Payload String payload, @Header(KafkaHeaders.RECEIVED_KEY) String key,
			@Header(name = "event-id", required = false) String eventId) {
		// TODO(service): replace logging with real handling + event-id dedupe.
		log.info("Consumed product.created key={} eventId={} payload={}", key, eventId, payload);
	}

}
