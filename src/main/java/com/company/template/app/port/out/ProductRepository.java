package com.company.template.app.port.out;

import com.company.template.domain.model.Product;
import java.util.List;
import java.util.Optional;

/** Outbound port: persistence SPI. Implemented by JPA or in-memory adapter. */
public interface ProductRepository {

	Product save(Product product);

	Optional<Product> findById(String id);

	List<Product> findAll(int page, int size);

}
