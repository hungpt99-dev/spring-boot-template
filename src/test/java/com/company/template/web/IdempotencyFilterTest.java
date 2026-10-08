package com.company.template.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.company.template.adapter.in.web.ProductController;
import com.company.template.app.port.in.ProductService;
import com.company.template.common.idempotency.IdempotencyFilter;
import com.company.template.common.idempotency.IdempotencyStore;
import com.company.template.domain.model.Product;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * Proves Idempotency-Key replay: second POST with the same key never reaches the service.
 */
class IdempotencyFilterTest {

	MockMvc mvc;

	CountingService service = new CountingService();

	@BeforeEach
	void setUp() {
		IdempotencyStore store = new IdempotencyStore.InMemoryIdempotencyStore(Duration.ofHours(24));
		ProductController controller = new ProductController(service);
		mvc = MockMvcBuilders.standaloneSetup(controller).addFilters(new IdempotencyFilter(store)).build();
	}

	@Test
	void repeatedKeyReplaysStoredResponse() throws Exception {
		String body = """
				{"name":"Widget","description":"d","priceCents":100}""";

		mvc.perform(post("/api/v1/products").contentType(MediaType.APPLICATION_JSON)
			.header("Idempotency-Key", "key-1")
			.content(body)).andExpect(status().isCreated());

		mvc.perform(post("/api/v1/products").contentType(MediaType.APPLICATION_JSON)
			.header("Idempotency-Key", "key-1")
			.content(body))
			.andExpect(status().isOk())
			.andExpect(header().string("Idempotency-Key", "replayed"))
			.andExpect(content().json("""
					{"name":"Widget"}"""));

		assert service.calls == 1 : "service must run once, ran " + service.calls;
	}

	static class CountingService implements ProductService {

		int calls;

		@Override
		public Product create(String name, String description, long priceCents) {
			calls++;
			return new Product("id-" + calls, name, description, priceCents, Instant.EPOCH);
		}

		@Override
		public Product get(String id) {
			throw new UnsupportedOperationException();
		}

		@Override
		public java.util.List<Product> list(int page, int size) {
			throw new UnsupportedOperationException();
		}

	}

}
