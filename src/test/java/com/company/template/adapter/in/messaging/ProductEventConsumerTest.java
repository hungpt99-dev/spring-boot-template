package com.company.template.adapter.in.messaging;

import org.junit.jupiter.api.Test;

/** The sample consumer must stay side-effect-free enough to call directly. */
class ProductEventConsumerTest {

	@Test
	void handlesEnvelopeWithoutThrowing() {
		new ProductEventConsumer().onProductCreated("{\"k\":\"v\"}", "id-1", "event-1");
		new ProductEventConsumer().onProductCreated("{\"k\":\"v\"}", "id-1", null);
	}

}
