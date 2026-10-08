package com.company.template;

import static org.assertj.core.api.Assertions.assertThat;

import com.company.template.adapter.out.messaging.KafkaProductEventPublisher;
import com.company.template.app.port.out.ProductEventPublisher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Proves publisher selection: with {@code kafka=true, outbox=false} the direct Kafka
 * publisher wires without needing a live broker (no connection at startup).
 */
@SpringBootTest
@TestPropertySource(properties = { "app.modules.kafka.enabled=true", "app.modules.outbox.enabled=false" })
class KafkaPublisherWiringTests {

	@Test
	void directPublisherWiresWithoutBroker(@Autowired ProductEventPublisher publisher) {
		assertThat(publisher).isInstanceOf(KafkaProductEventPublisher.class);
	}

}
