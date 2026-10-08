package com.company.template.adapter.in.web;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Inbound DTOs. Validation lives here (fail fast at the edge); domain stays
 * framework-free.
 */
public final class ProductDtos {

	private ProductDtos() {
	}

	@Schema(name = "CreateProductRequest", description = "Payload to create a product")
	public record CreateProductRequest(@NotBlank @Size(max = 200) String name, @Size(max = 2000) String description,
			@Min(0) long priceCents) {
	}

	@Schema(name = "ProductResponse", description = "Product representation")
	public record ProductResponse(String id, String name, String description, long priceCents, String createdAt) {
	}

}
