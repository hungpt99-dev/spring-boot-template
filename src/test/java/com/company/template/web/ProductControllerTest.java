package com.company.template.web;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.company.template.adapter.in.web.ProductController;
import com.company.template.app.port.in.ProductService;
import com.company.template.domain.model.Product;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Web-slice conventions: real HTTP + validation + error mapping, mocked use cases.
 * Idempotency filter disabled here (covered by {@code IdempotencyFilterTest}).
 */
@WebMvcTest(ProductController.class)
@org.springframework.test.context.TestPropertySource(properties = "app.modules.idempotency.enabled=false")
class ProductControllerTest {

	@Autowired
	MockMvc mvc;

	@MockitoBean
	ProductService service;

	private static final Product PRODUCT = new Product("id-1", "Widget", "A widget", 999,
			Instant.parse("2026-01-15T10:00:00Z"));

	@Test
	void createReturns201AndBody() throws Exception {
		when(service.create(eq("Widget"), anyString(), eq(999L))).thenReturn(PRODUCT);

		mvc.perform(post("/api/v1/products").contentType(MediaType.APPLICATION_JSON).content("""
				{"name":"Widget","description":"A widget","priceCents":999}"""))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.id").value("id-1"))
			.andExpect(jsonPath("$.name").value("Widget"))
			.andExpect(header().exists("X-Correlation-Id"));
	}

	@Test
	void createRejectsInvalidBodyWithProblemDetails() throws Exception {
		mvc.perform(post("/api/v1/products").contentType(MediaType.APPLICATION_JSON)
			.content("{\"name\":\"\",\"priceCents\":-1}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("TEMPLATE_VALIDATION_FAILED"))
			.andExpect(jsonPath("$.correlationId").exists());
	}

	@Test
	void getReturns404ProblemWhenMissing() throws Exception {
		when(service.get("missing")).thenThrow(new NoSuchElementException("Product not found: missing"));

		mvc.perform(get("/api/v1/products/missing"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("TEMPLATE_NOT_FOUND"));
	}

	@Test
	void listReturnsPage() throws Exception {
		when(service.list(anyInt(), anyInt())).thenReturn(List.of(PRODUCT));

		mvc.perform(get("/api/v1/products?page=0&size=20"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].id").value("id-1"));
	}

	@Test
	void illegalArgumentMapsTo400() throws Exception {
		when(service.list(anyInt(), anyInt())).thenThrow(new IllegalArgumentException("bad page"));

		mvc.perform(get("/api/v1/products?page=-1&size=20"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("TEMPLATE_BAD_REQUEST"));
	}

	@Test
	void generatedCorrelationIdIsPropagated() throws Exception {
		when(service.get("id-1")).thenReturn(PRODUCT);

		mvc.perform(get("/api/v1/products/id-1").header("X-Correlation-Id", "test-123"))
			.andExpect(status().isOk())
			.andExpect(header().string("X-Correlation-Id", "test-123"));
	}

}
