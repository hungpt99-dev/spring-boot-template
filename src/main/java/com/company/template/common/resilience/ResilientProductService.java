package com.company.template.common.resilience;

import com.company.template.app.port.in.ProductService;
import com.company.template.domain.model.Product;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * Resilience decorator over the plain {@link ProductService}. Active only when
 * {@code app.modules.resilience.enabled=true}. Uses programmatic decoration targets
 * (annotation-driven, no business-logic pollution): retry for reads, circuit breaker for
 * the list fan-out.
 */
@Service
@Primary
@ConditionalOnProperty(prefix = "app.modules", name = "resilience.enabled", havingValue = "true")
public class ResilientProductService implements ProductService {

	private final ProductService delegate;

	public ResilientProductService(@Qualifier("defaultProductService") ProductService delegate) {
		this.delegate = delegate;
	}

	@Override
	@Retry(name = "products")
	public Product create(String name, String description, long priceCents) {
		return delegate.create(name, description, priceCents);
	}

	@Override
	@Retry(name = "products")
	public Product get(String id) {
		return delegate.get(id);
	}

	@Override
	@CircuitBreaker(name = "products")
	public List<Product> list(int page, int size) {
		return delegate.list(page, size);
	}

}
