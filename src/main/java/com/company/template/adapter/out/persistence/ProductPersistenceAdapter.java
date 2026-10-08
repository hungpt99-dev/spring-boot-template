package com.company.template.adapter.out.persistence;

import com.company.template.app.port.out.OutboxStore;
import com.company.template.app.port.out.OutboxStore.OutboxMessage;
import com.company.template.app.port.out.ProductRepository;
import com.company.template.common.event.EventEnvelopeCodec;
import com.company.template.domain.model.Product;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA-backed port implementation. Active only when persistence is enabled. When the
 * outbox module is present, the domain event row is appended in the SAME transaction
 * (atomic publish intent); {@code OutboxRelay} delivers it.
 */
@Component
@ConditionalOnProperty(prefix = "app.modules", name = "persistence.enabled", havingValue = "true")
public class ProductPersistenceAdapter implements ProductRepository {

	private final SpringDataProductRepository jpa;

	private final ObjectProvider<OutboxStore> outbox;

	private final ObjectProvider<EventEnvelopeCodec> codec;

	private final String createdTopic;

	public ProductPersistenceAdapter(SpringDataProductRepository jpa, ObjectProvider<OutboxStore> outbox,
			ObjectProvider<EventEnvelopeCodec> codec,
			@Value("${app.messaging.product-created-topic:products.events.v1}") String createdTopic) {
		this.jpa = jpa;
		this.outbox = outbox;
		this.codec = codec;
		this.createdTopic = createdTopic;
	}

	@Override
	@Transactional
	public Product save(Product product) {
		ProductEntity saved = jpa.save(new ProductEntity(product.id(), product.name(), product.description(),
				product.priceCents(), product.createdAt()));
		Product domain = toDomain(saved);
		OutboxStore store = outbox.getIfAvailable();
		EventEnvelopeCodec envelopes = codec.getIfAvailable();
		if (store != null && envelopes != null) {
			store
				.append(new OutboxMessage(UUID.randomUUID(), "Product", domain.id(), "product.created.v1", createdTopic,
						envelopes.encode("product.created.v1", "Product", domain.id(),
								Map.of("name", domain.name(), "priceCents", domain.priceCents())),
						Instant.now(), null, 0));
		}
		return domain;
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Product> findById(String id) {
		return jpa.findById(id).map(this::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Product> findAll(int page, int size) {
		return jpa.findAll(PageRequest.of(page, size, Sort.by("name"))).stream().map(this::toDomain).toList();
	}

	private Product toDomain(ProductEntity e) {
		return new Product(e.getId(), e.getName(), e.getDescription(), e.getPriceCents(), e.getCreatedAt());
	}

}
