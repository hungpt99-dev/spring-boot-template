package com.company.template.app.service;

import com.company.template.app.port.in.ProductService;
import com.company.template.app.port.out.ProductEventPublisher;
import com.company.template.app.port.out.ProductRepository;
import com.company.template.domain.model.Product;
import java.time.Clock;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Default use-case implementation. Framework-free business logic except for the
 * {@code @Service} stereotype. Resilience (retry/circuit breaker) is applied by the
 * optional {@code ResilientProductService} decorator, not here.
 */
@Service
public class DefaultProductService implements ProductService {

	private final ProductRepository repository;

	private final ProductEventPublisher events;

	private final Clock clock;

	public DefaultProductService(ProductRepository repository, ProductEventPublisher events, Clock clock) {
		this.repository = repository;
		this.events = events;
		this.clock = clock;
	}

	@Override
	public Product create(String name, String description, long priceCents) {
		Product saved = repository
			.save(new Product(UUID.randomUUID().toString(), name, description, priceCents, clock.instant()));
		events.productCreated(saved);
		return saved;
	}

	@Override
	public Product get(String id) {
		return repository.findById(id).orElseThrow(() -> new NoSuchElementException("Product not found: " + id));
	}

	@Override
	public List<Product> list(int page, int size) {
		if (page < 0 || size < 1 || size > 200) {
			throw new IllegalArgumentException("Invalid pagination: page >= 0, 1 <= size <= 200");
		}
		return repository.findAll(page, size);
	}

}
