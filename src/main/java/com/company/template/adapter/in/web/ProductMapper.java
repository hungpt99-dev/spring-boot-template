package com.company.template.adapter.in.web;

import com.company.template.adapter.in.web.ProductDtos.ProductResponse;
import com.company.template.domain.model.Product;

/** Manual mapper. No mapping framework by design (explicit, debuggable, native-safe). */
public final class ProductMapper {

	private ProductMapper() {
	}

	public static ProductResponse toResponse(Product product) {
		return new ProductResponse(product.id(), product.name(), product.description(), product.priceCents(),
				product.createdAt() != null ? product.createdAt().toString() : null);
	}

}
