package com.company.template.app.port.out;

import com.company.template.domain.model.Product;

/** Outbound port: domain event publisher SPI. Implemented by Kafka or no-op adapter. */
public interface ProductEventPublisher {

	void productCreated(Product product);

}
