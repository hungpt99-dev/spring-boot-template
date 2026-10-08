package com.company.template.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.company.template.domain.model.Product;
import java.time.Instant;
import org.junit.jupiter.api.Test;

/** Covers the default (zero-infra) persistence adapter: ordering + pagination. */
class InMemoryProductRepositoryTest {

	private final InMemoryProductRepository repository = new InMemoryProductRepository();

	@Test
	void saveFindAndPagedList() {
		repository.save(new Product("b", "Banana", null, 200, Instant.EPOCH));
		repository.save(new Product("a", "Apple", null, 100, Instant.EPOCH));
		repository.save(new Product("c", "Cherry", null, 300, Instant.EPOCH));

		assertThat(repository.findById("a")).isPresent();
		assertThat(repository.findById("missing")).isEmpty();
		assertThat(repository.findAll(0, 2)).extracting(Product::name).containsExactly("Apple", "Banana");
		assertThat(repository.findAll(1, 2)).extracting(Product::name).containsExactly("Cherry");
		assertThat(repository.findAll(5, 2)).isEmpty();
	}

}
