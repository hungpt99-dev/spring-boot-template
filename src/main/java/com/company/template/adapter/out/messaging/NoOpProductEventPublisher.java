package com.company.template.adapter.out.messaging;

import com.company.template.app.port.out.ProductEventPublisher;
import com.company.template.domain.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fallback publisher: wired by {@link ProductEventPublisherConfig} whenever no other
 * {@link ProductEventPublisher} exists (Kafka off, or outbox on where the relay delivers
 * instead).
 */
public class NoOpProductEventPublisher implements ProductEventPublisher {

	private static final Logger log = LoggerFactory.getLogger(NoOpProductEventPublisher.class);

	@Override
	public void productCreated(Product product) {
		log.debug("No direct publisher, dropping productCreated event for {}", product.id());
	}

}
