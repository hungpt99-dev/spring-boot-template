package com.company.template.common.event;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

/** The envelope is the cross-service contract: pin its shape. */
class EventEnvelopeCodecTest {

	private final EventEnvelopeCodec codec = new EventEnvelopeCodec(new JsonMapper());

	@Test
	void envelopeContainsContractFields() throws Exception {
		String json = codec.encode("product.created.v1", "Product", "id-1", Map.of("name", "Widget"));

		var node = new JsonMapper().readTree(json);
		assertThat(node.get("eventId").asString()).isNotBlank();
		assertThat(node.get("type").asString()).isEqualTo("product.created.v1");
		assertThat(node.get("aggregateType").asString()).isEqualTo("Product");
		assertThat(node.get("aggregateId").asString()).isEqualTo("id-1");
		assertThat(node.get("occurredAt").asString()).isNotBlank();
		assertThat(node.get("payload").get("name").asString()).isEqualTo("Widget");
	}

}
