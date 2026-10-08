package com.company.template.domain.model;

import java.time.Instant;

/**
 * Sample aggregate. Pure domain object: no Spring, no Jakarta, no persistence
 * annotations. Replace with the service's real aggregates.
 */
public record Product(String id, String name, String description, long priceCents, Instant createdAt) {

	public Product {
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("Product name must not be blank");
		}
		if (priceCents < 0) {
			throw new IllegalArgumentException("priceCents must be >= 0");
		}
	}
}
