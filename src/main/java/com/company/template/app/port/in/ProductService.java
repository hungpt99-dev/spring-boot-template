package com.company.template.app.port.in;

import com.company.template.domain.model.Product;
import java.util.List;

/**
 * Inbound port: the application's use cases. Controllers depend on this, never on
 * adapters.
 */
public interface ProductService {

	Product create(String name, String description, long priceCents);

	Product get(String id);

	List<Product> list(int page, int size);

}
