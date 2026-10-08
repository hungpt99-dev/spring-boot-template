package com.company.template.adapter.out.messaging;

import com.company.template.app.port.out.ProductEventPublisher;
import com.company.template.common.event.EventEnvelopeCodec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * Publisher selection (exactly one {@link ProductEventPublisher}):
 * <ul>
 * <li>Kafka direct: {@code kafka=true} and {@code outbox=false} (at-most-once
 * intent).</li>
 * <li>Otherwise: no-op fallback (Kafka off, or outbox on where the relay delivers).</li>
 * </ul>
 */
@Configuration
public class ProductEventPublisherConfig {

	@Bean
	@ConditionalOnProperty(prefix = "app.modules.kafka", name = "enabled", havingValue = "true")
	@ConditionalOnProperty(prefix = "app.modules.outbox", name = "enabled", havingValue = "false",
			matchIfMissing = true)
	public ProductEventPublisher kafkaDirectPublisher(KafkaTemplate<String, String> kafka, EventEnvelopeCodec codec,
			@Value("${app.messaging.product-created-topic:products.events.v1}") String topic) {
		return new KafkaProductEventPublisher(kafka, codec, topic);
	}

	@Bean
	@ConditionalOnMissingBean(ProductEventPublisher.class)
	public ProductEventPublisher noOpPublisher() {
		return new NoOpProductEventPublisher();
	}

}
