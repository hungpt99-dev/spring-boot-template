package com.company.template.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.company.template.adapter.in.web.ProductDtos.ProductResponse;
import com.company.template.adapter.in.web.ProductMapper;
import com.company.template.domain.model.Product;
import java.time.Instant;
import org.junit.jupiter.api.Test;

/** Mapper is explicit by design; pin its contract. */
class ProductMapperTest {

	@Test
	void mapsDomainToResponse() {
		Product product = new Product("id-1", "Widget", "desc", 999, Instant.parse("2026-01-15T10:00:00Z"));

		ProductResponse response = ProductMapper.toResponse(product);

		assertThat(response.id()).isEqualTo("id-1");
		assertThat(response.createdAt()).isEqualTo("2026-01-15T10:00:00Z");
	}

	@Test
	void nullTimestampMapsToNull() {
		// Domain requires non-null createdAt only via service path; mapper stays
		// null-safe.
		Product product = new Product("id-1", "Widget", null, 0, Instant.EPOCH);
		assertThat(ProductMapper.toResponse(product).description()).isNull();
	}

}
