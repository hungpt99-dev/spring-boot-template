package com.company.template.adapter.out.messaging;

import com.company.template.app.port.out.ProductEventPublisher;
import com.company.template.common.event.EventEnvelopeCodec;
import com.company.template.domain.model.Product;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * Direct Kafka event publisher: at-most-once intent (no atomicity with the DB write).
 * Wired by {@link ProductEventPublisherConfig} only when
 * {@code app.modules.kafka.enabled=true} AND the outbox is off. For atomic publish,
 * enable the outbox module instead.
 */
public class KafkaProductEventPublisher implements ProductEventPublisher {

	private static final Logger log = LoggerFactory.getLogger(KafkaProductEventPublisher.class);

	private final KafkaTemplate<String, String> kafka;

	private final EventEnvelopeCodec codec;

	private final String topic;

	public KafkaProductEventPublisher(KafkaTemplate<String, String> kafka, EventEnvelopeCodec codec,
			@Value("${app.messaging.product-created-topic:products.events.v1}") String topic) {
		this.kafka = kafka;
		this.codec = codec;
		this.topic = topic;
	}

	@Override
	public void productCreated(Product product) {
		String payload = codec.encode("product.created.v1", "Product", product.id(),
				Map.of("name", product.name(), "priceCents", product.priceCents()));
		kafka.send(topic, product.id(), payload).whenComplete((result, ex) -> {
			if (ex != null) {
				log.error("Failed to publish productCreated for {}", product.id(), ex);
			}
		});
	}

}
