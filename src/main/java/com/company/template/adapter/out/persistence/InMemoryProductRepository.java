package com.company.template.adapter.out.persistence;

import com.company.template.app.port.out.ProductRepository;
import com.company.template.domain.model.Product;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

/**
 * Default persistence adapter: in-memory, zero infrastructure. Active when
 * {@code app.modules.persistence.enabled=false} (the default). The JPA adapter replaces
 * this bean when persistence is enabled.
 */
@Repository
@ConditionalOnProperty(prefix = "app.modules", name = "persistence.enabled", havingValue = "false",
		matchIfMissing = true)
public class InMemoryProductRepository implements ProductRepository {

	private final Map<String, Product> store = new ConcurrentHashMap<>();

	@Override
	public Product save(Product product) {
		store.put(product.id(), product);
		return product;
	}

	@Override
	public Optional<Product> findById(String id) {
		return Optional.ofNullable(store.get(id));
	}

	@Override
	public List<Product> findAll(int page, int size) {
		return store.values()
			.stream()
			.sorted(Comparator.comparing(Product::name))
			.skip((long) page * size)
			.limit(size)
			.toList();
	}

}
